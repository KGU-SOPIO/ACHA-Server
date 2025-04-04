package sopio.acha.domain.course.application;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestCourse;
import static sopio.acha.common.handler.ExtractorHandler.requestTimetable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.utils.CourseDataConverter;
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
	private final ActivityExtractor activityExtractor;
	private final MemberRepository memberRepository;
	private final ObjectMapper objectMapper;

	@Transactional(propagation = REQUIRES_NEW)
	public void extractCourseAndSave(Member currentMember) {
		try {
			String decryptedPassword = decrypt(currentMember.getPassword());

			// 시간표 데이터 요청 및 변환
			JsonNode timetableData = objectMapper.readTree(
					requestTimetable(currentMember.getId(), decryptedPassword)).get("data");
			List<CourseTimeTableResponse> timetableList = CourseDataConverter.convertToTimetableResponseList(timetableData, objectMapper);
			Map<String, CourseTimeTableResponse> timetableMap = CourseDataConverter.mapTimetableByIdentifier(timetableList);

			// 강좌 데이터 요청 및 변환
			JsonNode courseData = objectMapper.readTree(
					requestCourse(currentMember.getId(), decryptedPassword)).get("data");
			List<CourseBasicInformationResponse> courseList = CourseDataConverter.convertToCourseResponseList(courseData, objectMapper);
			Map<String, CourseBasicInformationResponse> courseMap = courseList.stream()
					.collect(Collectors.toMap(CourseBasicInformationResponse::identifier, Function.identity()));

			// 강좌 데이터 저장
			saveNewCourses(courseMap);

			// 공지사항 데이터 처리
			noticeExtractor.extractAndSave(courseMap);

			// 시간표 데이터 처리
			updateCourseWithTimetable(timetableMap);

			// 멤버 - 강좌 연결 저장
			List<Course> courseWithTimetable = getCoursesFromTimetable(timetableList);
			memberCourseService.saveMyCourses(courseWithTimetable, currentMember);

			// 활동 데이터 처리
			activityExtractor.extractAndSave(objectMapper, courseData, courseWithTimetable, currentMember);
			currentMember.updateExtract(true);

			memberRepository.save(currentMember);
		} catch (JsonProcessingException e) {
			throw new FailedParsingCourseDataException();
		}
	}

	private void saveNewCourses(Map<String, CourseBasicInformationResponse> courseMap) {
		Set<String> existingIdentifiers = courseRepository.findAllByIdentifierIn(courseMap.keySet())
				.stream()
				.map(Course::getIdentifier)
				.collect(Collectors.toSet());

		List<Course> newCourseList = courseMap.values().stream()
				.filter(courseResponse -> !existingIdentifiers.contains(courseResponse.identifier()))
				.map(courseResponse -> Course.save(
						courseResponse.title(),
						courseResponse.identifier(),
						courseResponse.code(),
						courseResponse.noticeCode(),
						courseResponse.professor()))
				.collect(Collectors.toList());

		if (!newCourseList.isEmpty()) {
			courseRepository.saveAll(newCourseList);
		}
	}

	private void updateCourseWithTimetable(Map<String, CourseTimeTableResponse> timetableMap) {
		List<Course> courses = courseRepository.findAllByIdentifierIn(timetableMap.keySet());
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

	private List<Course> getCoursesFromTimetable(List<CourseTimeTableResponse> timetableList) {
        Set<String> identifiers = timetableList.stream()
				.map(CourseTimeTableResponse::identifier)
				.collect(Collectors.toSet());
		Map<String, Course> courseMap = courseRepository.findAllByIdentifierIn(identifiers)
				.stream()
				.collect(Collectors.toMap(Course::getIdentifier, Function.identity()));

		return timetableList.stream()
				.map(timetable -> {
					Course course = courseMap.get(timetable.identifier());
					if (course == null) {
						throw new CourseNotFoundException();
					}
					return course;
				})
				.collect(Collectors.toList());
	}

	public Course getCourseByCode(String code) {
		return courseRepository.findByCode(code)
			.orElseThrow(CourseNotFoundException::new);
	}
}
