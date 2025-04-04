package sopio.acha.common.utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.presentation.response.CourseBasicInformationResponse;
import sopio.acha.domain.course.presentation.response.CourseTimeTableResponse;

public final class CourseDataConverter {
    private CourseDataConverter() {}

    public static List<CourseBasicInformationResponse> convertToCourseResponseList(JsonNode courseData, ObjectMapper objectMapper) {
        return StreamSupport.stream(courseData.spliterator(), false)
                .map(node -> objectMapper.convertValue(node, CourseBasicInformationResponse.class))
                .toList();
    }

    public static List<CourseTimeTableResponse> convertToTimetableResponseList(JsonNode timetableData, ObjectMapper objectMapper) {
        return StreamSupport.stream(timetableData.spliterator(), false)
                .map(node -> objectMapper.convertValue(node, CourseTimeTableResponse.class))
                .toList();
    }

    public static Map<String, CourseTimeTableResponse> mapTimetableByIdentifier(List<CourseTimeTableResponse> timetableList) {
        return timetableList.stream()
                .collect(Collectors.toMap(CourseTimeTableResponse::identifier, Function.identity()));
    }

    public static void mapTimetableToCourses(Map<String, CourseTimeTableResponse> timetableMap, List<Course> courses) {
        courses.forEach(course -> {
            CourseTimeTableResponse timetable = timetableMap.get(course.getIdentifier());
            if (timetable != null) {
                course.setTimetable(
                        timetable.day(),
                        timetable.classTime(),
                        timetable.startAt(),
                        timetable.endAt(),
                        timetable.lectureRoom()
                );
            }
        });
    }
}
