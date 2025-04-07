package sopio.acha.common.utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sopio.acha.domain.course.presentation.response.CourseScrapingResponse;
import sopio.acha.domain.timetable.presentation.response.TimetableScrapingResponse;

public final class CourseDataConverter {
        private CourseDataConverter() {
        }

        public static List<TimetableScrapingResponse> convertToTimetableList(ObjectMapper objectMapper,
                        JsonNode timetableJsonData) {
                return StreamSupport.stream(timetableJsonData.spliterator(), false)
                                .map(node -> objectMapper.convertValue(node, TimetableScrapingResponse.class))
                                .toList();
        }

        public static Map<String, List<TimetableScrapingResponse>> mapTimetableByIdentifier(
                        List<TimetableScrapingResponse> timetableList) {
                return timetableList.stream()
                                .collect(Collectors.groupingBy(
                                                TimetableScrapingResponse::identifier));
        }

        public static List<CourseScrapingResponse> convertToCourseList(ObjectMapper objectMapper,
                        JsonNode courseJsonData) {
                return StreamSupport.stream(courseJsonData.spliterator(), false)
                                .map(node -> objectMapper.convertValue(node, CourseScrapingResponse.class))
                                .toList();
        }

        public static Map<String, CourseScrapingResponse> mapCourseByIdentifier(
                        List<CourseScrapingResponse> courseList) {
                return courseList.stream()
                                .collect(Collectors.toMap(
                                                CourseScrapingResponse::identifier,
                                                Function.identity()));
        }
}
