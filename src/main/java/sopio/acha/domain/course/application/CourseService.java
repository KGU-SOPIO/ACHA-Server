package sopio.acha.domain.course.application;

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
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.infrastructure.CourseRepository;
import sopio.acha.domain.course.presentation.exception.FailedParsingCourseDataException;
import sopio.acha.domain.course.presentation.exception.CourseNotFoundException;
import sopio.acha.domain.course.presentation.response.CourseBasicInformationResponse;
import sopio.acha.domain.course.presentation.response.CourseTimeTableResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.memberCourse.application.MemberCourseService;

@Service
@RequiredArgsConstructor
public class CourseService {
	private final CourseRepository courseRepository;
	private final MemberCourseService memberCourseService;
	private final NoticeExtractor noticeExtractor;
	private final TimetableExtractor timetableExtractor;
	private final ActivityExtractor activityExtractor;
	private final MemberRepository memberRepository;
	private final ObjectMapper objectMapper;

	@Transactional(propagation = REQUIRES_NEW)
	public void extractCourseAndSave(Member currentMember) {
		try {
			JsonNode timetableData = objectMapper.readTree(
					requestTimeTable(currentMember.getId(), decrypt(currentMember.getPassword()))).get("data");
			JsonNode courseData = objectMapper.readTree(
					requestCourse(currentMember.getId(), decrypt(currentMember.getPassword()))).get("data");

			// 강좌 정보 추출 및 저장
			Map<String, CourseBasicInformationResponse> courseMap = extractCourseMap(courseData);
			saveNewCourses(courseMap);

			// 공지사항 데이터 처리
			noticeExtractor.extractAndSave(courseMap);

			// 시간표 데이터 처리
			timetableExtractor.extractAndUpdate(timetableData);

			// 멤버 - 강좌 연결 저장
			List<Course> courseWithTimetable = getCoursesFromTimetable(timetableData);
			memberCourseService.saveMyCourses(courseWithTimetable, currentMember);

			// 활동 데이터 처리
			activityExtractor.extractAndSave(objectMapper, courseData, courseWithTimetable, currentMember);
			currentMember.updateExtract(true);
			memberRepository.save(currentMember);
		} catch (JsonProcessingException e) {
			throw new FailedParsingCourseDataException();
		}
	}

	private Map<String, CourseBasicInformationResponse> extractCourseMap(JsonNode courseData) {
		List<CourseBasicInformationResponse> courseList = StreamSupport.stream(courseData.spliterator(), false)
			.map(node -> objectMapper.convertValue(node, CourseBasicInformationResponse.class))
			.toList();
		return courseList.stream()
			.collect(Collectors.toMap(CourseBasicInformationResponse::identifier, Function.identity()));
	}

	private void saveNewCourses(Map<String, CourseBasicInformationResponse> courseMap) {
		List<Course> newCourseList = courseMap.values().stream()
			.map(course -> Course.save(course.title(), course.identifier(), course.code(), course.noticeCode(), course.professor()))
			.filter(course -> !courseRepository.existsByIdentifier(course.getIdentifier()))
			.toList();
		if (!newCourseList.isEmpty()) {
			courseRepository.saveAll(newCourseList);
		}
	}

	private List<Course> getCoursesFromTimetable(JsonNode timetableData) {
		List<CourseTimeTableResponse> timetableList = StreamSupport.stream(timetableData.spliterator(), false)
            .map(node -> objectMapper.convertValue(node, CourseTimeTableResponse.class))
            .toList();
        return timetableList.stream()
            .map(timeTable -> courseRepository.findByIdentifier(timeTable.identifier())
                .orElseThrow(CourseNotFoundException::new))
            .toList();
	}

	public Course getCourseByCode(String code) {
		return courseRepository.findByCode(code)
			.orElseThrow(CourseNotFoundException::new);
	}
}
