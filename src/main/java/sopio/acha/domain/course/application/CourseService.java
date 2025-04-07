package sopio.acha.domain.course.application;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestCourse;
import static sopio.acha.common.handler.ExtractorHandler.requestTimetable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.utils.CourseDataConverter;
import sopio.acha.domain.activity.application.ActivityService;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.domain.CourseDay;
import sopio.acha.domain.course.infrastructure.CourseRepository;
import sopio.acha.domain.course.presentation.exception.FailedParsingCourseDataException;
import sopio.acha.domain.course.presentation.exception.CourseNotFoundException;
import sopio.acha.domain.course.presentation.response.CourseScrapingResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.memberCourse.application.MemberCourseService;
import sopio.acha.domain.notification.application.NotificationService;
import sopio.acha.domain.timetable.domain.Timetable;
import sopio.acha.domain.timetable.presentation.response.TimetableScrapingResponse;

@Service
@RequiredArgsConstructor
public class CourseService {
	private final ActivityService activityService;
	private final NotificationService notificationService;
	private final CourseRepository courseRepository;
	private final MemberCourseService memberCourseService;
	private final MemberRepository memberRepository;
	private final ObjectMapper objectMapper;

	@Transactional(propagation = REQUIRES_NEW)
	public void extractCourseAndSave(Member member) {
		try {
			String decryptedPassword = decrypt(member.getPassword());

			// 시간표 데이터 요청 및 변환
			JsonNode timetableJsonData = objectMapper.readTree(
					requestTimetable(member.getId(), decryptedPassword)).get("data");
			List<TimetableScrapingResponse> timetableList = CourseDataConverter
					.convertToTimetableList(objectMapper, timetableJsonData);
			Map<String, List<TimetableScrapingResponse>> timetableMap = CourseDataConverter
					.mapTimetableByIdentifier(timetableList);

			// 강좌 데이터 요청 및 변환
			JsonNode courseJsonData = objectMapper.readTree(
					requestCourse(member.getId(), decryptedPassword)).get("data");
			List<CourseScrapingResponse> courseList = CourseDataConverter.convertToCourseList(objectMapper,
					courseJsonData);
			Map<String, CourseScrapingResponse> courseMap = CourseDataConverter.mapCourseByIdentifier(courseList);

			// 신규 강좌 데이터 저장
			saveNewCourses(courseMap);

			// 시간표 데이터 처리
			updateCourseWithTimetable(timetableMap);

			// 멤버 - 강좌 매핑
			memberCourseService.saveCoursesWithMember(courseList, member);

			// 공지사항 데이터 저장
			extractAndSaveNotification(courseMap);

			// 활동 데이터 저장
			extractAndSaveActivity(courseMap, member);

			member.setExtract(true);
			memberRepository.save(member);
		} catch (JsonProcessingException e) {
			throw new FailedParsingCourseDataException();
		}
	}

	/// DB에 저장된 강좌를 찾아 존재하지 않는다면 새로 추가합니다.
	public boolean saveNewCourses(Map<String, CourseScrapingResponse> courseMap) {
		Set<String> existingCourseIdentifiers = courseRepository.findAllByIdentifierIn(courseMap.keySet())
				.stream()
				.map(Course::getIdentifier)
				.collect(Collectors.toSet());

		List<Course> newCourseList = courseMap.values().stream()
				.filter(courseObject -> !existingCourseIdentifiers.contains(courseObject.identifier()))
				.map(courseObject -> Course.save(
						courseObject.title(),
						courseObject.identifier(),
						courseObject.code(),
						courseObject.noticeCode(),
						courseObject.professor()))
				.collect(Collectors.toList());

		if (!newCourseList.isEmpty()) {
			courseRepository.saveAll(newCourseList);
			return true;
		}
		return false;
	}

	/// DB에 저장된 강좌를 찾아 시간표 데이터를 업데이트 합니다.
	/// day, startAt로 조회했을 때, 시간표 데이터가 이미 존재한다면 시간표를 업데이트합니다.
	/// 일치하는 시간표 정보가 없다면 새로 추가합니다.
	public void updateCourseWithTimetable(Map<String, List<TimetableScrapingResponse>> timetableMap) {
		List<Course> courses = courseRepository.findAllByIdentifierIn(timetableMap.keySet());
		courses.forEach(course -> {
			List<TimetableScrapingResponse> responses = timetableMap.get(course.getIdentifier());
			if (responses != null && !responses.isEmpty()) {
				responses.forEach(response -> {
					Optional<Timetable> existingTimetableOpt = course.getTimetables().stream()
							.filter(timetable -> timetable.getDay().equals(CourseDay.valueOf(response.day()))
									&& timetable.getStartAt() == response.startAt())
							.findFirst();
					if (existingTimetableOpt.isPresent()) {
						Timetable existingTimetable = existingTimetableOpt.get();
						existingTimetable.setClassTime(response.classTime());
						existingTimetable.setEndAt(response.endAt());
						existingTimetable.setLectureRoom(response.lectureRoom());
					} else {
						course.addTimetable(
								response.day(),
								response.classTime(),
								response.startAt(),
								response.endAt(),
								response.lectureRoom());
					}
				});
			}
		});
	}

	/// 공지사항 데이터를 추출해 DB에 저장합니다.
	/// course, title, link로 조회했을 때, 공지사항 데이터가 이미 존재한다면 공지사항을 업데이트합니다.
	/// 일치하는 공지사항 정보가 없다면 새로 추가합니다.
	public void extractAndSaveNotification(Map<String, CourseScrapingResponse> courseMap) {
		courseMap.values().stream()
				.filter(courseObject -> courseObject.notices() != null && !courseObject.notices().isEmpty())
				.forEach(courseObject -> {
					Course course = courseRepository.findByIdentifier(courseObject.identifier())
							.orElseThrow(CourseNotFoundException::new);
					notificationService.saveOrUpdateNotification(courseObject.notices(), course);
				});
	}

	/// 활동 데이터를 추출해 DB에 저장합니다.
	/// title, week, type, member, course로 조회했을 때, 활동 데이터가 이미 존재한다면 활동을 업데이트합니다.
	/// 일치하는 활동 정보가 없다면 새로 추가합니다.
	public void extractAndSaveActivity(Map<String, CourseScrapingResponse> courseMap, Member member) {
		courseMap.values().forEach(courseObject -> {
			if (courseObject.activities() == null || courseObject.activities().isEmpty())
				return;

			Optional<Course> optionalCourse = courseRepository.findByIdentifier(courseObject.identifier());
			if (optionalCourse.isEmpty())
				return;

			Course course = optionalCourse.get();
			courseObject.activities().forEach(weekResponse -> {
				int week = weekResponse.week();
				weekResponse.activities().forEach(activityObject -> {
					activityService.saveOrUpdateActivity(course, member, week, activityObject);
				});
			});
		});
	}
}
