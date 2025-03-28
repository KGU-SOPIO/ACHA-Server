package sopio.acha.domain.lecture.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.infrastructure.LectureRepository;
import sopio.acha.domain.lecture.presentation.exception.LectureNotFoundException;
import sopio.acha.domain.lecture.presentation.response.LectureTimeTableResponse;

@Component
@RequiredArgsConstructor
public class TimetableExtractor {
    private final LectureRepository lectureRepository;
    private final ObjectMapper objectMapper;

    public void extractAndUpdate(JsonNode timetableData) {
        List<LectureTimeTableResponse> timetableList = StreamSupport.stream(timetableData.spliterator(), false)
			.map(node -> objectMapper.convertValue(node, LectureTimeTableResponse.class))
			.toList();
		Map<String, LectureTimeTableResponse> timetableMap = timetableList.stream()
			.collect(Collectors.toMap(LectureTimeTableResponse::identifier, Function.identity()));

		timetableMap.forEach((identifier, timetable) -> {
			Lecture course = lectureRepository.findByIdentifier(identifier)
				.orElseThrow(LectureNotFoundException::new);
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
