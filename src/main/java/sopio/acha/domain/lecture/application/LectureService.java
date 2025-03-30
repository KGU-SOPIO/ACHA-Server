package sopio.acha.domain.lecture.application;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestCourse;
import static sopio.acha.common.handler.ExtractorHandler.requestTimeTable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.infrastructure.LectureRepository;
import sopio.acha.domain.lecture.presentation.exception.FailedParsingLectureDataException;
import sopio.acha.domain.lecture.presentation.exception.LectureNotFoundException;
import sopio.acha.domain.lecture.presentation.response.LectureBasicInformationResponse;
import sopio.acha.domain.lecture.presentation.response.LectureTimeTableResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.application.MemberLectureService;

@Service
@RequiredArgsConstructor
public class LectureService {
	private final LectureRepository lectureRepository;
	private final MemberLectureService memberLectureService;
	private final NoticeExtractor noticeExtractor;
	private final TimetableExtractor timetableExtractor;
	private final ActivityExtractor activityExtractor;
	private final ObjectMapper objectMapper;

	@Transactional(propagation = REQUIRES_NEW)
	public void extractCourseAndSave(Member currentMember) {
		try {
			JsonNode timetableData = objectMapper.readTree(
					requestTimeTable(currentMember.getId(), decrypt(currentMember.getPassword()))).get("data");
			JsonNode courseData = objectMapper.readTree(
					requestCourse(currentMember.getId(), decrypt(currentMember.getPassword()))).get("data");

			// 강좌 정보 추출 및 저장
			Map<String, LectureBasicInformationResponse> courseMap = extractCourseMap(courseData);
			saveNewCourses(courseMap);

			// 공지사항 데이터 처리
			noticeExtractor.extractAndSave(courseMap);

			// 시간표 데이터 처리
			timetableExtractor.extractAndUpdate(timetableData);

			// 멤버 - 강좌 연결 저장
			List<Lecture> courseWithTimetable = getCoursesFromTimetable(timetableData);
			memberLectureService.saveMyLectures(courseWithTimetable, currentMember);

			// 활동 데이터 처리
			activityExtractor.extractAndSave(objectMapper, courseData, courseWithTimetable, currentMember);
		} catch (JsonProcessingException e) {
			throw new FailedParsingLectureDataException();
		}
	}

	private Map<String, LectureBasicInformationResponse> extractCourseMap(JsonNode courseData) {
		List<LectureBasicInformationResponse> courseList = StreamSupport.stream(courseData.spliterator(), false)
			.map(node -> objectMapper.convertValue(node, LectureBasicInformationResponse.class))
			.toList();
		return courseList.stream()
			.collect(Collectors.toMap(LectureBasicInformationResponse::identifier, Function.identity()));
	}

	private void saveNewCourses(Map<String, LectureBasicInformationResponse> courseMap) {
		List<Lecture> newCourseList = courseMap.values().stream()
			.map(course -> Lecture.save(course.title(), course.identifier(), course.code(), course.noticeCode(), course.professor()))
			.filter(course -> !lectureRepository.existsByIdentifier(course.getIdentifier()))
			.toList();
		if (!newCourseList.isEmpty()) {
			lectureRepository.saveAll(newCourseList);
		}
	}

	private List<Lecture> getCoursesFromTimetable(JsonNode timetableData) {
		List<LectureTimeTableResponse> timetableList = StreamSupport.stream(timetableData.spliterator(), false)
            .map(node -> objectMapper.convertValue(node, LectureTimeTableResponse.class))
            .toList();
        return timetableList.stream()
            .map(timeTable -> lectureRepository.findByIdentifier(timeTable.identifier())
                .orElseThrow(LectureNotFoundException::new))
            .toList();
	}

	public Lecture getLectureByCode(String code) {
		return lectureRepository.findByCode(code)
			.orElseThrow(LectureNotFoundException::new);
	}
}
