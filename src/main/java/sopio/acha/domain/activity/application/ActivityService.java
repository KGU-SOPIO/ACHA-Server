package sopio.acha.domain.activity.application;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestActivity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.domain.ActivityType;
import sopio.acha.domain.activity.domain.SubmitType;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.exception.FailedParsingActivityDataException;
import sopio.acha.domain.activity.presentation.exception.FailedScheduleActivityEventException;
import sopio.acha.domain.activity.presentation.response.ActivityResponse;
import sopio.acha.domain.activity.presentation.response.ActivitySummaryListResponse;
import sopio.acha.domain.activity.presentation.response.ActivityWeekListResponse;
import sopio.acha.domain.fcm.application.FcmService;
import sopio.acha.domain.course.application.CourseService;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberCourse.application.MemberCourseService;
import sopio.acha.domain.memberCourse.domain.MemberCourse;

@Service
@RequiredArgsConstructor
public class ActivityService {
	private final ActivityRepository activityRepository;
	private final CourseService courseService;
	private final FcmService fcmService;

	@Transactional
	@Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
	public void sendActivityNotificationToFCM() {
		LocalDateTime now = LocalDateTime.now();
		List<Activity> activities = activityRepository.findAllByDeadlineAfterAndNotifyScheduledIsFalse(now);
		for (Activity activity : activities) {
			LocalDateTime deadline = activity.getDeadline();
			try {
				long hoursUntilDeadline = Duration.between(now, deadline).toHours();

				if (hoursUntilDeadline > 72) {
					scheduleNotification(activity, activity.getTitle() + " 마감 기한이 3일 남았어요", deadline.minusDays(3));
					scheduleNotification(activity, activity.getTitle() + " 마감 기한이 1일 남았어요", deadline.minusDays(1));
					scheduleNotification(activity, activity.getTitle() + " 마감 기한이 1시간 남았어요", deadline.minusHours(1));
				} else if (hoursUntilDeadline > 24) {
					scheduleNotification(activity, activity.getTitle() + " 마감 기한이 1일 남았어요", deadline.minusDays(1));
					scheduleNotification(activity, activity.getTitle() + " 마감 기한이 1시간 남았어요", deadline.minusHours(1));
				} else if (hoursUntilDeadline > 1) {
					scheduleNotification(activity, activity.getTitle() + " 마감 기한이 1시간 남았어요", deadline.minusHours(1));
				}
				activity.updateNotifyScheduledTrue();
			} catch (Exception e) {
				throw new FailedScheduleActivityEventException();
			}
		}
	}

	@Transactional
	public ActivitySummaryListResponse getMyActivityList(Member currentMember) {
		try {
			Pageable topTen = PageRequest.of(0, 10);
			List<Activity> activities = activityRepository.findLectureAndAssignmentActivities(
					currentMember.getId(),
					LocalDateTime.now(),
					ActivityType.ASSIGNMENT,
					ActivityType.LECTURE,
					SubmitType.NONE,
					topTen
			);
			return ActivitySummaryListResponse.from(activities);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	@Transactional
	public ActivityWeekListResponse getCourseActivityList(Member currentMember, String code) {
		Course targetCourse = courseService.getCourseByCode(code);
		List<Activity> activities = activityRepository.findAllByMemberIdAndCourseIdOrderByWeekAsc(
			currentMember.getId(),
			targetCourse.getId()
		);

		Map<Integer, List<Activity>> groupedActivities = activities.stream()
			.collect(Collectors.groupingBy(Activity::getWeek));

		return ActivityWeekListResponse.from(targetCourse, groupedActivities);
	}

	private void scheduleNotification(Activity activity, String description, LocalDateTime triggerTime) {
		if (triggerTime.isAfter(LocalDateTime.now())) {
			fcmService.saveFcmEvent(activity.getMember(), activity.getCourse().getTitle(), description, triggerTime);
		}
	}
}
