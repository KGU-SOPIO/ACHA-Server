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
import java.util.Optional;
import java.util.stream.StreamSupport;


@Component
@RequiredArgsConstructor
public class LectureServiceHelper {
    private final MemberLectureService memberLectureService;
    private final ActivityRepository activityRepository;

    public void saveLectureAndActivities(JsonNode courseData, List<Lecture> lectureHasTimeTable, Member currentMember, ObjectMapper objectMapper) {
        List<Activity> activities = new ArrayList<>();

        List<MemberLecture> memberLectures = lectureHasTimeTable.stream()
                .filter(lecture -> !memberLectureService.isExistsMemberLecture(currentMember, lecture))
                .map(lecture -> new MemberLecture(currentMember, lecture))
                .toList();

        for (MemberLecture memberLecture : memberLectures) {
            for (JsonNode dataNode : courseData) {
                JsonNode activitiesWrapper = dataNode.get("activities");
                if (activitiesWrapper == null || !activitiesWrapper.isArray())
                    continue;

                for (JsonNode weekNode : activitiesWrapper) {
                    int week = weekNode.get("week").asInt();
                    JsonNode activityArray = weekNode.get("activities");
                    if (activityArray == null || !activityArray.isArray())
                        continue;

                    activities.addAll(StreamSupport.stream(activityArray.spliterator(), false)
                        .map(node -> objectMapper.convertValue(node, ActivityResponse.class))
                        .filter(activityResponse -> !isExistsActivity(activityResponse.title(),
                            memberLecture.getMember().getId()))
                        .map(activityResponse -> {
                            Activity activity = Activity.save(
                                activityResponse.available(),
                                0,
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
                            );
                            return activity;
                        })
                        .toList());
                }
            }
            activityRepository.saveAll(activities);
        }
    }

    private boolean isExistsActivity(String title, String memberId) {
        return activityRepository.existsActivityByTitleAndMemberId(title, memberId);
    }
}
