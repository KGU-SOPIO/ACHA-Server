package sopio.acha.domain.activity.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.domain.ActivityType;
import sopio.acha.domain.activity.domain.SubmitType;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.response.ActivitySummaryListResponse;
import sopio.acha.domain.activity.presentation.response.ActivityWeekListResponse;
import sopio.acha.domain.fcm.application.FcmService;
import sopio.acha.domain.course.application.CourseService;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.member.domain.Member;

@Service
@RequiredArgsConstructor
public class ActivityService {
	private final ActivityRepository activityRepository;
	private final CourseService courseService;
	private final FcmService fcmService;

	@Transactional
	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
	public void scheduledCheckAndSendActivityNotification() {
		scheduledActivityNotificationCheckAndSend();
	}

	public void scheduledActivityNotificationCheckAndSend() {
		LocalDateTime now = LocalDateTime.now();
		List<Activity> activityList = activityRepository.findAllByDeadlineAfter(
				now,
				now.plusDays(4),
				ActivityType.ASSIGNMENT,
				ActivityType.LECTURE,
				SubmitType.NONE
		);

		for (Activity activity : activityList) {
			LocalDateTime deadline = activity.getDeadline();
			if (deadline == null) continue;

			if (!activity.isNotifiedThreeDays() &&
					now.isAfter(deadline.minusDays(3)) &&
					now.isBefore(deadline.minusDays(2))) {
				String message = activity.getTitle() + " 마감 기한이 3일 남았어요";
				fcmService.sendNotificationToMember(activity.getMember(), activity.getCourse().getTitle(), message);
				activity.updateNotifiedThreeDays(true);
			}
			if (!activity.isNotifiedOneDay() &&
					now.isAfter(deadline.minusDays(1)) &&
					now.isBefore(deadline.minusHours(6))) {
				String message = activity.getTitle() + " 마감 기한이 1일 남았어요";
				fcmService.sendNotificationToMember(activity.getMember(), activity.getCourse().getTitle(), message);
				activity.updateNotifiedOneDay(true);
			}
			if (!activity.isNotifiedOneHour() && now.isAfter(deadline.minusHours(1))) {
				String message = activity.getTitle() + " 마감 기한이 1시간 남았어요";
				fcmService.sendNotificationToMember(activity.getMember(), activity.getCourse().getTitle(), message);
				activity.updateNotifiedOneHour(true);
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
}
