package sopio.acha.domain.lecture.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.response.ActivityResponse;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.application.MemberLectureService;
import sopio.acha.domain.memberLecture.domain.MemberLecture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Component
@RequiredArgsConstructor
public class ExtractActivities {
    private final MemberLectureService memberLectureService;
    private final ActivityRepository activityRepository;

    public void saveLectureAndActivities(JsonNode courseData, List<Lecture> courseWithTimeTable, Member currentMember, ObjectMapper objectMapper) {
        List<Activity> activities = new ArrayList<>();
        String memberId = currentMember.getId();

        List<MemberLecture> memberCourses = courseWithTimeTable.stream()
                .filter(course -> !memberLectureService.isExistsMemberLecture(currentMember, course))
                .map(course -> new MemberLecture(currentMember, course))
                .toList();

        Map<String, MemberLecture> memberCourseMap = memberCourses.stream()
                .collect(Collectors.toMap(memberCourse -> memberCourse.getLecture().getIdentifier(), Function.identity()));

        for (JsonNode dataNode : courseData) {
            String identifier = dataNode.get("identifier").asText();
            JsonNode activitiesWrapper = dataNode.get("activities");
            if (activitiesWrapper == null || activitiesWrapper.isArray()) {
                continue;
            }

            MemberLecture memberCourse = memberCourseMap.get(identifier);
            for (JsonNode weekNode : activitiesWrapper) {
                int week = weekNode.get("week").asInt();
                JsonNode activityArray = weekNode.get("activities");
                if (activityArray == null || activityArray.isArray()) {
                    continue;
                }

                List<ActivityResponse> activityResponses = StreamSupport.stream(activityArray.spliterator(), false)
                        .map(node -> objectMapper.convertValue(node, ActivityResponse.class))
                        .toList();

                activityResponses.stream()
                        .filter(activityResponse -> !isExistsActivity(activityResponse.title(), memberId))
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
                                memberCourse.getLecture(),
                                memberCourse.getMember()
                        )).forEach(activities::add);
            }
        }
        if (!activities.isEmpty()) {
            activityRepository.saveAll(activities);
        }
    }

    private boolean isExistsActivity(String title, String memberId) {
        return activityRepository.existsActivityByTitleAndMemberId(title, memberId);
    }
}
