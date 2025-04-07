package sopio.acha.domain.memberCourse.application;

import static sopio.acha.common.handler.DateHandler.getTodayDate;
import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.exception.ExtractorErrorException;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.common.utils.CourseDataConverter;
import sopio.acha.domain.activity.application.ActivityService;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingResponse;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingWeekResponse;
import sopio.acha.domain.course.application.CourseService;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.domain.CourseDay;
import sopio.acha.domain.course.infrastructure.CourseRepository;
import sopio.acha.domain.course.presentation.response.CourseScrapingResponse;
import sopio.acha.domain.timetable.domain.Timetable;
import sopio.acha.domain.timetable.presentation.response.TimetableScrapingResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberCourse.domain.MemberCourse;
import sopio.acha.domain.memberCourse.infrastructure.MemberCourseRepository;
import sopio.acha.domain.memberCourse.presentation.response.MemberCourseListResponse;

@Service
@RequiredArgsConstructor
public class MemberCourseService {
	private final MemberCourseRepository memberCourseRepository;
	private final ActivityRepository activityRepository;
	private final CourseRepository courseRepository;
	private final CourseService courseService;
	private final ActivityService activityService;

	@Transactional
	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
	public void scheduledExtractCourse() {
		scheduledCourseExtraction();
	}

	public void scheduledCourseExtraction() {
		ObjectMapper objectMapper = new ObjectMapper();

		// 업데이트 주기가 지난 사용자 강좌 조회
		List<MemberCourse> allMemberCourseList = memberCourseRepository
				.findAllByCourseYearAndCourseSemester(DateHandler.getCurrentSemesterYear(),
						DateHandler.getCurrentSemester())
				.stream()
				.filter(MemberCourse::checkLastUpdatedAt)
				.peek(memberCourse -> memberCourse.setLastUpdatedAt(LocalDateTime.now()))
				.toList();

		// 강좌 업데이트
		updateExtractedCourse(allMemberCourseList, objectMapper);
	}

	private void updateExtractedCourse(List<MemberCourse> currentCourseList, ObjectMapper objectMapper) {
		// 사용자별 강좌 그룹화
		Map<Member, List<MemberCourse>> memberCourseMap = currentCourseList.stream()
				.collect(Collectors.groupingBy(MemberCourse::getMember));

		System.out.println("업데이트 대상 사용자 수 : " + memberCourseMap.size());

		for (Member member : memberCourseMap.keySet()) {
			String decryptedPassword = decrypt(member.getPassword());
			List<MemberCourse> memberCourseList = memberCourseMap.get(member);

			// 추출 가능 상태 검증 및 실패 시 모든 강좌 업데이트 대기 적용
			try {
				requestAuthentication(member.getId(), decryptedPassword);
			} catch (ExtractorErrorException e) {
				memberCourseList
						.forEach(memberCourse -> memberCourse.setLastUpdatedAt(LocalDateTime.now().plusHours(3)));
				continue;
			}

			// 강좌 목록 요청 및 변환
			List<CourseScrapingResponse> lmsCourseList = fetchCourseListResponse(member, decryptedPassword,
					objectMapper);
			if (lmsCourseList == null || lmsCourseList.isEmpty()) {
				continue;
			}
			Map<String, CourseScrapingResponse> lmsCourseMap = CourseDataConverter.mapCourseByIdentifier(lmsCourseList);

			// 신규 강좌 저장 및 결과 반환
			boolean isNewCourseSaved = courseService.saveNewCourses(lmsCourseMap);

			// 시간표 데이터 요청 및 강좌 매핑
			if (isNewCourseSaved) {
				// 시간표 데이터 요청 및 변환
				List<TimetableScrapingResponse> timetableList = fetchTimetableListResponse(member, decryptedPassword,
						objectMapper);
				Map<String, List<TimetableScrapingResponse>> timetableMap = CourseDataConverter
						.mapTimetableByIdentifier(timetableList);

				// 시간표 데이터 저장
				courseService.updateCourseWithTimetable(timetableMap);

				// Member - Course 매핑
				saveCoursesWithMember(lmsCourseList, member);
			}

			// LMS에 존재하지 않는 강의 삭제
			Set<String> lmsIdentifiers = lmsCourseList.stream()
					.map(CourseScrapingResponse::identifier)
					.collect(Collectors.toSet());
			Set<String> memberCourseIdentifiers = memberCourseList.stream()
					.map(memberCourse -> memberCourse.getCourse().getIdentifier())
					.collect(Collectors.toSet());
			Set<String> identifiersToRemove = new HashSet<>(memberCourseIdentifiers);
			identifiersToRemove.removeAll(lmsIdentifiers);
			Iterator<MemberCourse> iterator = memberCourseList.iterator();
			while (iterator.hasNext()) {
				MemberCourse memberCourse = iterator.next();
				if (identifiersToRemove.contains(memberCourse.getCourse().getIdentifier())) {
					// 활동 삭제
					activityRepository.deleteAllByMemberAndCourse(member, memberCourse.getCourse());
					// MemberCourse 삭제
					memberCourseRepository.delete(memberCourse);
					iterator.remove();
				}
			}

			for (CourseScrapingResponse summaryResponse : lmsCourseList) {
				CourseScrapingResponse detailedResponse = fetchCourseDetailResponse(member, decryptedPassword,
						summaryResponse.code(), objectMapper);
				if (detailedResponse == null)
					continue;

				Map<String, CourseScrapingResponse> detailedCourseMap = CourseDataConverter
						.mapCourseByIdentifier(Collections.singletonList(detailedResponse));

				courseService.extractAndSaveNotification(detailedCourseMap);

				List<ActivityScrapingWeekResponse> weekResponses = detailedResponse.activities();
				if (weekResponses != null && !weekResponses.isEmpty()) {
					for (ActivityScrapingWeekResponse weekResponse : weekResponses) {
						int week = weekResponse.week();
						for (ActivityScrapingResponse activityResponse : weekResponse.activities()) {
							Optional<Course> optionalCourse = courseRepository
									.findByIdentifier(detailedResponse.identifier());
							if (optionalCourse.isPresent()) {
								Course course = optionalCourse.get();
								activityService.saveOrUpdateActivity(course, member, week, activityResponse);
							}
						}
					}
				}
			}
		}
	}

	/// 사용자의 강좌 목록 데이터를 요청합니다.
	/// 공지사항, 활동 데이터는 포함되지 않습니다.
	private List<CourseScrapingResponse> fetchCourseListResponse(Member member, String decryptedPassword,
			ObjectMapper objectMapper) {
		try {
			JsonNode courseListJsonData = objectMapper.readTree(requestCourseList(member.getId(), decryptedPassword))
					.get("data");
			return CourseDataConverter
					.convertToCourseList(objectMapper, courseListJsonData);
		} catch (Exception e) {
			return null;
		}
	}

	/// 사용자의 특정 강좌 데이터를 요청합니다.
	private CourseScrapingResponse fetchCourseDetailResponse(Member member, String decryptedPassword, String courseCode,
			ObjectMapper objectMapper) {
		try {
			JsonNode courseDetailJsonData = objectMapper
					.readTree(requestCourseDetail(member.getId(), decryptedPassword, courseCode)).get("data");
			return objectMapper.convertValue(courseDetailJsonData, CourseScrapingResponse.class);
		} catch (Exception e) {
			return null;
		}
	}

	/// 시간표 데이터를 요청합니다.
	private List<TimetableScrapingResponse> fetchTimetableListResponse(Member member, String decryptedPassword,
			ObjectMapper objectMapper) {
		try {
			JsonNode timetableJsonData = objectMapper
					.readTree(requestTimetable(member.getId(), decryptedPassword)).get("data");
			return CourseDataConverter.convertToTimetableList(objectMapper, timetableJsonData);
		} catch (Exception e) {
			return null;
		}
	}

	/// Course와 Member를 매핑합니다.
	/// 이미 매핑된 경우나 Course가 존재하지 않는 경우에는 저장하지 않습니다.
	public void saveCoursesWithMember(List<CourseScrapingResponse> courseList, Member member) {
		Set<String> identifiers = courseList.stream()
				.map(CourseScrapingResponse::identifier)
				.collect(Collectors.toSet());
		List<Course> courses = courseRepository.findAllByIdentifierIn(identifiers);
		List<MemberCourse> memberCourses = courses.stream()
				.filter(course -> !memberCourseRepository.existsByMemberIdAndCourseId(member.getId(), course.getId()))
				.map(course -> new MemberCourse(member, course))
				.toList();
		memberCourseRepository.saveAll(memberCourses);
	}

	@Transactional(readOnly = true)
	public MemberCourseListResponse getTodayMemberCourse(Member member) {
		List<MemberCourse> memberCourses = memberCourseRepository
				.findAllByMemberIdAndCourseYearAndCourseSemester(member.getId(), DateHandler.getCurrentSemesterYear(),
						DateHandler.getCurrentSemester());

		// 오늘 날짜 강좌 조회
		CourseDay today = CourseDay.valueOf(getTodayDate());
		List<MemberCourse> todayMemberCourses = memberCourses.stream()
				.filter(memberCourse -> memberCourse.getCourse().getTimetables().stream()
						.anyMatch(timetable -> timetable.getDay().equals(today)))
				.toList();

		// 강좌 시간순 정렬
		todayMemberCourses.sort(Comparator.comparing(mc -> mc.getCourse().getTimetables().stream()
				.filter(timetable -> timetable.getDay().equals(today))
				.map(timetable -> timetable.getStartAt())
				.findFirst()
				.orElse(Integer.MAX_VALUE)));
		return MemberCourseListResponse.from(todayMemberCourses);
	}

	@Transactional(readOnly = true)
	public MemberCourseListResponse getThisSemesterMemberCourse(Member member) {
		List<MemberCourse> memberCourses = memberCourseRepository.findAllByMemberIdAndCourseYearAndCourseSemester(
				member.getId(), DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester());

		memberCourses.sort(
				Comparator.comparing((MemberCourse mc) -> mc.getCourse().getTimetables().stream()
						.filter(timetable -> timetable.getDay() != null)
						.map(Timetable::getDayOrder)
						.min(Integer::compare)
						.orElse(Integer.MAX_VALUE))
						.thenComparing(mc -> mc.getCourse().getTimetables().stream()
								.filter(timetable -> timetable.getDay() != null)
								.map(Timetable::getStartAt)
								.min(Integer::compare)
								.orElse(Integer.MAX_VALUE)));
		return MemberCourseListResponse.from(memberCourses);
	}
}
