package sopio.acha.domain.lecture.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.response.ActivityResponse;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.application.MemberLectureService;
import sopio.acha.domain.memberLecture.domain.MemberLecture;

@Component
@RequiredArgsConstructor
public class ActivityExtractor {
    private final MemberLectureService memberLectureService;
    private final ActivityRepository activityRepository;

    public void extractAndSave(ObjectMapper objectMapper, JsonNode courseData, List<Lecture> courseWithtimetable, Member member) {
        List<Activity> activities = new ArrayList<>();
        String memberId = member.getId();

        List<MemberLecture> memberCourses = courseWithtimetable.stream()
                .filter(course -> !memberLectureService.isExistsMemberLecture(member, course))
                .map(course -> new MemberLecture(member, course))
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
            if (memberCourse== null) {
                continue;
            }

            for (JsonNode weekNode : activitiesWrapper) {
                int week = weekNode.get("week").asInt();
                JsonNode activityArray = weekNode.get("activities");
                if (activityArray == null || activityArray.isArray()) {
                    continue;
                }

                List<ActivityResponse> activityResponses = StreamSupport.stream(activityArray.spliterator(), false)
                        .map(node -> objectMapper.convertValue(node, ActivityResponse.class))
                        .toList();

                for (ActivityResponse activityResponse : activityResponses) {
                    if (!activityRepository.existsActivityByTitleAndMemberId(activityResponse.title(), memberId)) {
                        Activity activity = Activity.save(
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
                        );
                        activities.add(activity);
                    }
                }
            }
        }
        if (!activities.isEmpty()) {
            activityRepository.saveAll(activities);
        }
    }
}
