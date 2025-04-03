package sopio.acha.domain.memberCourse.application;

import static sopio.acha.common.handler.DateHandler.getTodayDate;
import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.exception.ExtractorErrorException;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingResponse;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingWeekResponse;
import sopio.acha.domain.course.application.NoticeExtractor;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.domain.CourseDay;
import sopio.acha.domain.course.presentation.exception.FailedParsingCourseDataException;
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

	@Transactional
	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
	public void scheduledExtractCourse() {
		scheduledCourseExtraction();
	}

	public void scheduledCourseExtraction() {
		ObjectMapper objectMapper = new ObjectMapper();
		List<MemberCourse> allCourseList = getAllMemberCourse()
				.stream()
				.filter(MemberCourse::checkLastUpdatedAt)
				.peek(MemberCourse::setLastUpdatedAt)
				.toList();
		updateExtractedCourse(allCourseList, objectMapper);
	}

	private void updateExtractedCourse(List<MemberCourse> currentCourseList, ObjectMapper objectMapper) {
		Map<Member, List<MemberCourse>> memberCourseMap = currentCourseList.stream()
				.collect(Collectors.groupingBy(MemberCourse::getMember));

		memberCourseMap.forEach((member, memberCourses) -> {
			String decryptedPassword = decrypt(member.getPassword());
			try {
				JsonNode courseData = objectMapper.readTree(
						requestCourse(member.getId(), decryptedPassword)).get("data");
				if (courseData == null || !courseData.isArray()) {
					return;
				}

				// 강좌 데이터 변환
				List<CourseBasicInformationResponse> courseResponseList = StreamSupport
						.stream(courseData.spliterator(), false)
						.map(node -> objectMapper.convertValue(node, CourseBasicInformationResponse.class))
						.toList();

				// 강좌코드 추출
				Set<String> memberCourseIdentifiers = memberCourses.stream()
						.map(course -> course.getCourse().getIdentifier())
						.collect(Collectors.toSet());

				// 신규 강좌 필터링
				List<CourseBasicInformationResponse> newCourseResponseList = courseResponseList.stream()
						.filter(response -> !memberCourseIdentifiers.contains(response.identifier()))
						.toList();

				// 신규 강좌 시간표 매핑 후 저장 처리 필요
				if (!newCourseResponseList.isEmpty()) {
					List<Course> newCourses = newCourseResponseList.stream()
							.map(courseResponse -> Course.save(
									courseResponse.title(),
									courseResponse.identifier(),
									courseResponse.code(),
									courseResponse.noticeCode(),
									courseResponse.professor()))
							.toList();

					JsonNode timetableData = objectMapper.readTree(
							requestTimetable(member.getId(), decryptedPassword)).get("data");

					List<CourseTimeTableResponse> timetableList = StreamSupport.stream(timetableData.spliterator(), false)
							.map(node -> objectMapper.convertValue(node, CourseTimeTableResponse.class))
							.toList();

					Map<String, CourseTimeTableResponse> timetableMap = timetableList.stream()
							.collect(Collectors.toMap(CourseTimeTableResponse::identifier, Function.identity()));
					newCourses.forEach(course -> {
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

					// 신규 강좌 등록
					saveMyCourses(newCourses, member);
					List<MemberCourse> newMemberCourses = newCourses.stream()
							.map(course -> new MemberCourse(member, course))
							.toList();
					memberCourses.addAll(newMemberCourses);

					// 식별자 집합 업데이트
					newCourses.forEach(course -> memberCourseIdentifiers.add(course.getIdentifier()));
				}

				// 강좌 Map 생성
				Map<String, CourseBasicInformationResponse> courseResponseMap = courseResponseList.stream()
						.filter(response -> memberCourseIdentifiers.contains(response.identifier()))
						.collect(Collectors.toMap(CourseBasicInformationResponse::identifier, Function.identity()));

				// 공지사항 업데이트
				noticeExtractor.extractAndSave(courseResponseMap);

				for (CourseBasicInformationResponse courseResponse : courseResponseList) {
					Optional<MemberCourse> optionalMemberCourse = memberCourses.stream()
							.filter(course -> course.getCourse().getIdentifier().equals(courseResponse.identifier()))
							.findFirst();
					if (optionalMemberCourse.isEmpty()) {
						continue;
					}
					Course course = optionalMemberCourse.get().getCourse();

					List<ActivityScrapingWeekResponse> weekResponses = courseResponse.activities();
					for (ActivityScrapingWeekResponse weekResponse : weekResponses) {
						int week = weekResponse.week();
						for (ActivityScrapingResponse activityResponse : weekResponse.activities()) {
							if (!activityRepository.existsActivityByTitleAndMemberId(activityResponse.title(), member.getId())) {
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
						}
					}
				}
			} catch (JsonProcessingException e) {
				throw new FailedParsingCourseDataException();
			}
		});
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
		memberCourses.sort(Comparator.comparing(mc -> mc.getCourse().getStartAt()));
		return MemberCourseListResponse.from(memberCourses);
	}

	@Transactional(readOnly = true)
	public MemberCourseListResponse getThisSemesterMemberCourse(Member currentMember) {
		List<MemberCourse> memberCourses = memberCourseRepository.findAllByMemberIdAndCourseYearAndCourseSemesterOrderByCourseDayOrderAsc(
			currentMember.getId(), DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester());
		memberCourses.sort(
			Comparator.comparing((MemberCourse mc) -> mc.getCourse().getDayOrder())
				.thenComparing(mc -> mc.getCourse().getStartAt())
		);
		return MemberCourseListResponse.from(memberCourses);
	}

	public List<MemberCourse> getAllMemberCourse() {
		List<MemberCourse> memberCourseList = memberCourseRepository
			.findAllByCourseYearAndCourseSemesterOrderByCourseDayOrderAsc(
				DateHandler.getCurrentSemesterYear(),
				DateHandler.getCurrentSemester()
			);

		return memberCourseList.stream()
			.filter(memberCourse -> {
				try {
					requestAuthentication(
						memberCourse.getMember().getId(),
						decrypt(memberCourse.getMember().getPassword())
					);
					return true;
				} catch (ExtractorErrorException e) {
					return false;
				}
			})
			.toList();
	}


	public boolean isExistsMemberCourse(Member currentMember, Course course) {
		return !memberCourseRepository.existsByMemberAndCourse(currentMember, course);
	}
}
