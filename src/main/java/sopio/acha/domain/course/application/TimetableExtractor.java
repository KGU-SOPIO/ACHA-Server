package sopio.acha.domain.course.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.infrastructure.CourseRepository;
import sopio.acha.domain.course.presentation.exception.CourseNotFoundException;
import sopio.acha.domain.course.presentation.response.CourseTimeTableResponse;

@Component
@RequiredArgsConstructor
public class TimetableExtractor {
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    public void extractAndUpdate(JsonNode timetableData) {
        List<CourseTimeTableResponse> timetableList = StreamSupport.stream(timetableData.spliterator(), false)
			.map(node -> objectMapper.convertValue(node, CourseTimeTableResponse.class))
			.toList();
		Map<String, CourseTimeTableResponse> timetableMap = timetableList.stream()
			.collect(Collectors.toMap(CourseTimeTableResponse::identifier, Function.identity()));

		timetableMap.forEach((identifier, timetable) -> {
			Course course = courseRepository.findByIdentifier(identifier)
				.orElseThrow(CourseNotFoundException::new);
			course.setTimeTable(
				timetable.day(),
				timetable.classTime(),
				timetable.startAt(),
				timetable.endAt(),
				timetable.lectureRoom()
			);
		});
    }
}
