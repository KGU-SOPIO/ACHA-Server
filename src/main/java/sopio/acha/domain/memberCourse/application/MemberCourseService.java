package sopio.acha.domain.memberCourse.application;

import static sopio.acha.common.handler.DateHandler.getTodayDate;
import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.exception.ExtractorErrorException;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.common.utils.CourseDataConverter;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.domain.ActivityType;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingResponse;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingWeekResponse;
import sopio.acha.domain.course.application.NoticeExtractor;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.domain.CourseDay;
import sopio.acha.domain.course.infrastructure.CourseRepository;
import sopio.acha.domain.course.presentation.response.CourseBasicInformationResponse;
import sopio.acha.domain.course.presentation.response.CourseTimeTableResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberCourse.domain.MemberCourse;
import sopio.acha.domain.memberCourse.infrastructure.MemberCourseRepository;
import sopio.acha.domain.memberCourse.presentation.response.MemberCourseListResponse;

@Service
@RequiredArgsConstructor
public class MemberCourseService {
	private final MemberCourseRepository memberCourseRepository;
	private final NoticeExtractor noticeExtractor;
	private final ActivityRepository activityRepository;
	private final CourseRepository courseRepository;

	@Transactional
	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
	public void scheduledExtractCourse() {
		scheduledCourseExtraction();
	}

	public void scheduledCourseExtraction() {
		ObjectMapper objectMapper = new ObjectMapper();

		// 업데이트 주기가 지난 사용자 강좌 조회
		List<MemberCourse> allMemberCourseList = memberCourseRepository
				.findAllByCourseYearAndCourseSemesterOrderByCourseDayOrderAsc(
						DateHandler.getCurrentSemesterYear(),
						DateHandler.getCurrentSemester()
				)
				.stream()
				.filter(MemberCourse::checkLastUpdatedAt)
				.peek(MemberCourse::setLastUpdatedAt)
				.toList();

		// 강좌 업데이트
		updateExtractedCourse(allMemberCourseList, objectMapper);
	}

	private void updateExtractedCourse(List<MemberCourse> currentCourseList, ObjectMapper objectMapper) {
		// 사용자 별 강좌 그룹화
		Map<Member, List<MemberCourse>> memberCourseMap = currentCourseList.stream()
				.collect(Collectors.groupingBy(MemberCourse::getMember));

		System.out.println("업데이트 대상 사용자 수: " + memberCourseMap.size());

		// 사용자 순회하며 업데이트
		for (Member member : memberCourseMap.keySet()) {
			String decryptedPassword = decrypt(member.getPassword());
			List<MemberCourse> memberCourseList = memberCourseMap.get(member);

			// 추출 가능 상태 확인
			try {
				requestAuthentication(member.getId(), decryptedPassword);
			} catch (ExtractorErrorException e) {
				continue;
			}

			// 강좌 데이터 요청
			List<CourseBasicInformationResponse> courseResponseList = fetchCourseResponses(member, decryptedPassword, objectMapper);
			if (courseResponseList == null || courseResponseList.isEmpty()) {
				continue;
			}

			// 강좌 코드 추출
			Set<String> memberCourseIdentifiers = extractMemberCourseIdentifiers(member);

			// 등록되어 있지 않은 새로운 강좌 저장
			saveNewCoursesForMember(member, memberCourseList, courseResponseList, memberCourseIdentifiers, decryptedPassword, objectMapper);

			// 강좌 데이터 변환
			Map<String, CourseBasicInformationResponse> courseResponseMap = courseResponseList.stream()
					.filter(response -> memberCourseIdentifiers.contains(response.identifier()))
					.collect(Collectors.toMap(CourseBasicInformationResponse::identifier, Function.identity()));

			// 공지사항 저장
			try {
				noticeExtractor.extractAndSave(courseResponseMap);
			} catch (Exception _) {}

			updateActivitiesForMember(member, memberCourseList, courseResponseList);
		}
	}

	// 사용자 강좌 스크래핑 데이터 요청
	private List<CourseBasicInformationResponse> fetchCourseResponses(Member member, String decryptedPassword, ObjectMapper objectMapper) {
		try {
			JsonNode courseData = objectMapper.readTree(requestCourse(member.getId(), decryptedPassword)).get("data");
			if (courseData == null || !courseData.isArray()) {
				return null;
			}
			return CourseDataConverter.convertToCourseResponseList(courseData, objectMapper);
		} catch (Exception e) {
			return null;
		}
	}

	// 사용자 수강 강좌 식별자 집합 생성
	private Set<String> extractMemberCourseIdentifiers(Member member) {
		List<MemberCourse> allMemberCourseList = memberCourseRepository.findAllByMemberIdAndCourseYearAndCourseSemester(
				member.getId(),
				DateHandler.getCurrentSemesterYear(),
				DateHandler.getCurrentSemester()
		);

		return allMemberCourseList.stream()
				.map(memberCourse -> memberCourse.getCourse().getIdentifier())
				.collect(Collectors.toSet());
	}

	// 신규 강좌 등록 및 시간표 매핑
	private void saveNewCoursesForMember(Member member, List<MemberCourse> memberCourseList,
										 List<CourseBasicInformationResponse> courseResponseList,
										 Set<String> memberCourseIdentifiers, String decryptedPassword,
										 ObjectMapper objectMapper) {
		List<CourseBasicInformationResponse> newCourseResponseList = courseResponseList.stream()
				.filter(response -> !memberCourseIdentifiers.contains(response.identifier()))
				.toList();
		if (newCourseResponseList.isEmpty()) return;

		List<Course> newCourseList = newCourseResponseList.stream()
				.map(courseResponse -> {
					Optional<Course> existingCourse = courseRepository.findByIdentifier(courseResponse.identifier());
                    return existingCourse.orElseGet(() -> Course.save(
                            courseResponse.title(),
                            courseResponse.identifier(),
                            courseResponse.code(),
                            courseResponse.noticeCode(),
                            courseResponse.professor()
                    ));
				}).toList();

		try	{
			JsonNode timetableData = objectMapper.readTree(requestTimetable(member.getId(), decryptedPassword)).get("data");
			if (timetableData != null && timetableData.isArray()) {
				List<CourseTimeTableResponse> timetableList = CourseDataConverter.convertToTimetableResponseList(timetableData, objectMapper);
				Map<String, CourseTimeTableResponse> timetableMap = CourseDataConverter.mapTimetableByIdentifier(timetableList);

				// 신규 강좌 시간표 매핑
				CourseDataConverter.mapTimetableToCourses(timetableMap, newCourseList);
			}
		} catch (JsonProcessingException _) {}

		// 사용자 강좌 저장
		courseRepository.saveAll(newCourseList);
		saveMyCourses(newCourseList, member);
		List<MemberCourse> newMemberCourseList = newCourseList.stream()
				.map(course -> new MemberCourse(member, course))
				.toList();
		memberCourseList.addAll(newMemberCourseList);

		// 신규 강좌 식별자 업데이트
		newCourseList.forEach(course -> memberCourseIdentifiers.add(course.getIdentifier()));
	}

	private void updateActivitiesForMember(Member member, List<MemberCourse> memberCourseList	,
										   List<CourseBasicInformationResponse> courseResponseList) {
		for (CourseBasicInformationResponse courseResponse : courseResponseList) {
			Optional<MemberCourse> optionalMemberCourse = memberCourseList.stream()
					.filter(course -> course.getCourse().getIdentifier().equals(courseResponse.identifier()))
					.findFirst();
			if (optionalMemberCourse.isEmpty()) continue;

			Course course = optionalMemberCourse.get().getCourse();
			List<ActivityScrapingWeekResponse> weekResponses = courseResponse.activities();
			if (weekResponses == null || weekResponses.isEmpty()) continue;

			for (ActivityScrapingWeekResponse weekResponse : weekResponses) {
				int week = weekResponse.week();
				for (ActivityScrapingResponse activityResponse : weekResponse.activities()) {
					try {
						Optional<Activity> existingActivityOpt = activityRepository.findByTitleAndWeekAndMemberIdAndCourseAndType(
								activityResponse.title(),
								week,
								member.getId(),
								course,
								ActivityType.valueOf(activityResponse.type().toUpperCase()));
						if (existingActivityOpt.isPresent()) {
							Activity existingActivity = existingActivityOpt.get();
							existingActivity.update(
									activityResponse.available(),
									activityResponse.link(),
									activityResponse.attendance(),
									activityResponse.submitStatus(),
									activityResponse.startAt(),
									activityResponse.deadline(),
									activityResponse.timeLeft(),
									activityResponse.description()
							);
							activityRepository.save(existingActivity);
						} else {
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
									course,
									member
							);
							activityRepository.save(activity);
						}
					} catch (Exception _) {}
				}
			}
		}
	}

	public void saveMyCourses(List<Course> courseHasTimeTable, Member currentMember) {
		List<MemberCourse> memberCourses = courseHasTimeTable.stream()
			.filter(course -> isExistsMemberCourse(currentMember, course))
			.map(course -> new MemberCourse(currentMember, course))
			.toList();
		memberCourseRepository.saveAll(memberCourses);
	}

	@Transactional(readOnly = true)
	public MemberCourseListResponse getTodayMemberCourse(Member currentMember) {
		CourseDay today = CourseDay.valueOf(getTodayDate());
		List<MemberCourse> memberCourses = memberCourseRepository.findAllByMemberIdAndCourseDayAndCourseYearAndCourseSemester(
			currentMember.getId(), today, DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester());
		memberCourses.sort(Comparator.comparing(memberCourse -> memberCourse.getCourse().getStartAt()));
		return MemberCourseListResponse.from(memberCourses);
	}

	@Transactional(readOnly = true)
	public MemberCourseListResponse getThisSemesterMemberCourse(Member currentMember) {
		List<MemberCourse> memberCourses = memberCourseRepository.findAllByMemberIdAndCourseYearAndCourseSemester(
			currentMember.getId(), DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester());
		memberCourses.sort(
			Comparator.comparing((MemberCourse memberCourse) -> memberCourse.getCourse().getDayOrder(),
					Comparator.nullsLast(Comparator.naturalOrder())
					).thenComparing(memberCourse -> memberCourse.getCourse().getStartAt(),
							Comparator.nullsLast(Comparator.naturalOrder())
					)
		);
		return MemberCourseListResponse.from(memberCourses);
	}

	public boolean isExistsMemberCourse(Member currentMember, Course course) {
		return !memberCourseRepository.existsByMemberAndCourse(currentMember, course);
	}
}
