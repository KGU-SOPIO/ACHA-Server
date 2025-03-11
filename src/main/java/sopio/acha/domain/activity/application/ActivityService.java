package sopio.acha.domain.activity.application;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestActivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.exception.FailedParsingActivityDataException;
import sopio.acha.domain.activity.presentation.response.ActivityResponse;
import sopio.acha.domain.activity.presentation.response.ActivitySummaryListResponse;
import sopio.acha.domain.activity.presentation.response.ActivityWeekListResponse;
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

	@Transactional
	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
	public void scheduledExtractActivity() {
		scheduledActivityExtraction();
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
		return ActivityWeekListResponse.from(targetLecture, activities);
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
