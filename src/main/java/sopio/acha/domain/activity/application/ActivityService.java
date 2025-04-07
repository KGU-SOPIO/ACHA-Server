package sopio.acha.domain.activity.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import sopio.acha.domain.activity.presentation.response.ActivityScrapingResponse;
import sopio.acha.domain.activity.presentation.response.ActivitySummaryListResponse;
import sopio.acha.domain.activity.presentation.response.ActivityWeekListResponse;
import sopio.acha.domain.fcm.application.FcmService;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.infrastructure.CourseRepository;
import sopio.acha.domain.course.presentation.exception.CourseNotFoundException;
import sopio.acha.domain.member.domain.Member;

@Service
@RequiredArgsConstructor
public class ActivityService {
	private final ActivityRepository activityRepository;
	private final CourseRepository courseRepository;
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
				SubmitType.NONE);

		for (Activity activity : activityList) {
			LocalDateTime deadline = activity.getDeadline();
			if (deadline == null)
				continue;

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

	/// 활동 데이터를 조회하여 존재하면 업데이트하고, 존재하지 않으면 새로 생성합니다.
	/// title, week, type, member, course를 기준으로 활동을 조회합니다.
	@Transactional
	public void saveOrUpdateActivity(Course course, Member member, int week,
			ActivityScrapingResponse activityResponse) {
		Optional<Activity> optionalActivity = activityRepository.findByTitleAndWeekAndTypeAndCourseIdAndMemberId(
				activityResponse.title(), week, ActivityType.valueOf(activityResponse.type().toUpperCase()),
				course.getId(), member.getId());

		if (optionalActivity.isPresent()) {
			Activity activity = optionalActivity.get();
			activity.update(
					activityResponse.available(),
					activityResponse.link(),
					activityResponse.deadline(),
					activityResponse.timeLeft(),
					activityResponse.description(),
					activityResponse.attendance(),
					activityResponse.submitStatus());
		} else {
			Activity newActivity = Activity.save(
					activityResponse.available(),
					week,
					activityResponse.title(),
					activityResponse.link(),
					activityResponse.type(),
					activityResponse.code(),
					activityResponse.deadline(),
					activityResponse.startAt(),
					activityResponse.lectureTime(),
					activityResponse.timeLeft(),
					activityResponse.description(),
					activityResponse.attendance(),
					activityResponse.submitStatus(),
					course,
					member);
			activityRepository.save(newActivity);
		}
	}

	@Transactional
	public ActivitySummaryListResponse getMyActivityList(Member currentMember) {
		Pageable topTen = PageRequest.of(0, 10);
		List<Activity> activities = activityRepository.findLectureAndAssignmentActivities(
				currentMember.getId(),
				LocalDateTime.now(),
				ActivityType.ASSIGNMENT,
				ActivityType.LECTURE,
				SubmitType.NONE,
				topTen);
		return ActivitySummaryListResponse.from(activities);
	}

	@Transactional
	public ActivityWeekListResponse getCourseActivityList(Member currentMember, String code) {
		Course targetCourse = courseRepository.findByCode(code)
				.orElseThrow(CourseNotFoundException::new);
		List<Activity> activities = activityRepository.findAllByMemberIdAndCourseIdOrderByWeekAsc(
				currentMember.getId(),
				targetCourse.getId());

		Map<Integer, List<Activity>> groupedActivities = activities.stream()
				.collect(Collectors.groupingBy(Activity::getWeek));

		return ActivityWeekListResponse.from(targetCourse, groupedActivities);
	}
}
