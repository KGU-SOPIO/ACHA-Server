package sopio.acha.domain.course.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingResponse;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingWeekResponse;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberCourse.application.MemberCourseService;
import sopio.acha.domain.memberCourse.domain.MemberCourse;

@Component
@RequiredArgsConstructor
public class ActivityExtractor {
    private final MemberCourseService memberCourseService;
    private final ActivityRepository activityRepository;

    public void extractAndSave(ObjectMapper objectMapper, JsonNode courseData, List<Course> courseWithtimetable, Member member) {
        List<Activity> activities = new ArrayList<>();
        String memberId = member.getId();

        List<MemberCourse> memberCourses = courseWithtimetable.stream()
                .filter(course -> !memberCourseService.isExistsMemberCourse(member, course))
                .map(course -> new MemberCourse(member, course))
                .toList();

        Map<String, MemberCourse> memberCourseMap = memberCourses.stream()
                .collect(Collectors.toMap(memberCourse -> memberCourse.getCourse().getIdentifier(), Function.identity()));

        for (JsonNode dataNode : courseData) {
            String identifier = dataNode.get("identifier").asText();
            JsonNode activitiesNode = dataNode.get("activities");
            if (activitiesNode == null || !activitiesNode.isArray()) {
                continue;
            }
            MemberCourse memberCourse = memberCourseMap.get(identifier);
            if (memberCourse == null) {
                continue;
            }

            for (JsonNode weekNode : activitiesNode) {
                ActivityScrapingWeekResponse weekResponse = objectMapper.convertValue(weekNode, ActivityScrapingWeekResponse.class);
                int week = weekResponse.week();
                for (ActivityScrapingResponse activityResponse : weekResponse.activities()) {
                    if (!activityRepository.existsActivityByTitleAndMemberId(activityResponse.title(), memberId)) {
                        Activity activity = Activity.save(
                            activityResponse.available(),
                            week,
                            activityResponse.title(),
                            activityResponse.link(),
                            activityResponse.type(),
                            activityResponse.code(),
                            activityResponse.deadline(),
                            activityResponse.startAt(),
                            activityResponse.courseTime(),
                            activityResponse.timeLeft(),
                            activityResponse.description(),
                            activityResponse.attendance(),
                            activityResponse.submitStatus(),
                            memberCourse.getCourse(),
                            memberCourse.getMember()
                        );
                        activities.add(activity);
                    }
                }
            }
            if (!activities.isEmpty()) {
                activityRepository.saveAll(activities);
            }
        }
    }
}
