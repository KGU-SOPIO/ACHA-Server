package sopio.acha.domain.activity.application;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.exception.FailedParsingActivityDataException;
import sopio.acha.domain.activity.presentation.response.ActivityResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.application.MemberLectureService;
import sopio.acha.domain.memberLecture.domain.MemberLecture;

@Service
@RequiredArgsConstructor
public class ActivityService {
	private final ActivityRepository activityRepository;
	private final MemberLectureService memberLectureService;

	@Transactional
	public void extractActivity(Member currentMember) {
		ObjectMapper objectMapper = new ObjectMapper();
		List<MemberLecture> currentLectures = memberLectureService.getCurrentMemberLectureAndSetLastUpdatedAt(currentMember);

		try {
			for (MemberLecture memberLecture : currentLectures) {
				JsonNode responseNode = objectMapper.readTree(
					requestActivity(currentMember.getId(), decrypt(currentMember.getPassword()),
						memberLecture.getLecture().getCode()));

				JsonNode dataNodes = responseNode.get("data");
				if (dataNodes == null || !dataNodes.isArray()) continue;
				List<Activity> activities = new ArrayList<>();

				for (JsonNode dataNode : dataNodes) {
					int week = dataNode.get("week").asInt();
					JsonNode activitiesNode = dataNode.get("activities");
					if (activitiesNode == null || !activitiesNode.isArray()) continue;

					activities.addAll(StreamSupport.stream(activitiesNode.spliterator(), false)
						.map(node -> objectMapper.convertValue(node, ActivityResponse.class))
						.filter(activityResponse -> !isExistsActivity(activityResponse.title(), currentMember.getId()))
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
							currentMember
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
