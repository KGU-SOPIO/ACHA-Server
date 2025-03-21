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

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.exception.FailedParsingActivityDataException;
import sopio.acha.domain.activity.presentation.exception.FailedScheduleActivityEventException;
import sopio.acha.domain.activity.presentation.response.ActivityResponse;
import sopio.acha.domain.activity.presentation.response.ActivitySummaryListResponse;
import sopio.acha.domain.activity.presentation.response.ActivityWeekListResponse;
import sopio.acha.domain.fcm.application.FcmService;
import sopio.acha.domain.fcm.domain.Device;
import sopio.acha.domain.lecture.application.LectureService;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.application.MemberLectureService;
import sopio.acha.domain.memberLecture.domain.MemberLecture;

@Service
@RequiredArgsConstructor
public class ActivityService {
	private final ActivityRepository activityRepository;
	private final MemberLectureService memberLectureService;
	private final LectureService lectureService;
	private final FcmService fcmService;

	@Transactional
	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
	public void scheduledExtractActivity() {
		scheduledActivityExtraction();
	}

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
	public void extractActivity(Member currentMember) {
		ObjectMapper objectMapper = new ObjectMapper();
		List<MemberLecture> currentLectures = memberLectureService.getCurrentMemberLectureAndSetLastUpdatedAt(
			currentMember);
		saveExtractedActivity(currentLectures, objectMapper);
	}

	public void scheduledActivityExtraction() {
		ObjectMapper objectMapper = new ObjectMapper();
		List<MemberLecture> allLectureList = memberLectureService.getAllMemberLecture()
			.stream()
			.filter(MemberLecture::checkLastUpdatedAt)
			.peek(MemberLecture::setLastUpdatedAt)
			.toList();
		saveExtractedActivity(allLectureList, objectMapper);
	}

	@Transactional
	public ActivitySummaryListResponse getMyActivityList(Member currentMember) {
		List<Activity> activities = activityRepository.findTop10ByMemberIdAndDeadlineAfterOrderByDeadlineAsc(
			currentMember.getId(), LocalDateTime.now());
		return ActivitySummaryListResponse.from(activities);
	}

	@Transactional
	public ActivityWeekListResponse getLectureActivityList(Member currentMember, String code) {
		Lecture targetLecture = lectureService.getLectureByCode(code);
		List<Activity> activities = activityRepository.findAllByMemberIdAndLectureIdOrderByWeekAsc(
			currentMember.getId(), targetLecture.getId());
		System.out.println(activities);
		Map<Integer, List<Activity>> groupedActivities = activities.stream()
			.collect(Collectors.groupingBy(Activity::getWeek));
		System.out.println(groupedActivities);
		return ActivityWeekListResponse.from(targetLecture, groupedActivities);
	}

	private void scheduleNotification(Activity activity, String description, LocalDateTime triggerTime) {
		if (triggerTime.isAfter(LocalDateTime.now())) {
			fcmService.saveFcmEvent(activity.getMember(), activity.getLecture().getTitle(), description, triggerTime);
		}
	}

	private void saveExtractedActivity(List<MemberLecture> currentLectures, ObjectMapper objectMapper) {
		try {
			for (MemberLecture memberLecture : currentLectures) {
				JsonNode responseNode = objectMapper.readTree(
					requestActivity(memberLecture.getMember().getId(), decrypt(memberLecture.getMember().getPassword()),
						memberLecture.getLecture().getCode()));

				JsonNode dataNodes = responseNode.get("data");
				if (dataNodes == null || !dataNodes.isArray())
					continue;
				List<Activity> activities = new ArrayList<>();

				for (JsonNode dataNode : dataNodes) {
					int week = dataNode.get("week").asInt();
					JsonNode activitiesNode = dataNode.get("activities");
					if (activitiesNode == null || !activitiesNode.isArray())
						continue;

					activities.addAll(StreamSupport.stream(activitiesNode.spliterator(), false)
						.map(node -> objectMapper.convertValue(node, ActivityResponse.class))
						.filter(activityResponse -> !isExistsActivity(activityResponse.title(),
							memberLecture.getMember().getId()))
						.map(activityResponse -> Activity.save(
							activityResponse.available(),
							week,
							activityResponse.title(),
							Optional.ofNullable(activityResponse.link()).orElse(""),
							activityResponse.type(),
							Optional.ofNullable(activityResponse.code()).orElse(""),
							Optional.ofNullable(activityResponse.deadline()).orElse(""),
							activityResponse.startAt(),
							activityResponse.lectureTime(),
							Optional.ofNullable(activityResponse.timeLeft()).orElse(""),
							Optional.ofNullable(activityResponse.description()).orElse(""),
							memberLecture.getLecture(),
							memberLecture.getMember()
						))
						.toList());
				}
				activityRepository.saveAll(activities);
			}
		} catch (JsonProcessingException e) {
			throw new FailedParsingActivityDataException();
		}
	}

	private boolean isExistsActivity(String title, String memberId) {
		return activityRepository.existsActivityByTitleAndMemberId(title, memberId);
	}
}
