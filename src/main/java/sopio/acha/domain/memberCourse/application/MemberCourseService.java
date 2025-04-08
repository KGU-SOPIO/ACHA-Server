package sopio.acha.domain.memberCourse.application;

import static sopio.acha.common.handler.DateHandler.getTodayDate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.course.domain.CourseDay;
import sopio.acha.domain.timetable.domain.Timetable;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberCourse.domain.MemberCourse;
import sopio.acha.domain.memberCourse.infrastructure.MemberCourseRepository;
import sopio.acha.domain.memberCourse.presentation.response.MemberCourseListResponse;

@Service
@RequiredArgsConstructor
public class MemberCourseService {
	private final MemberCourseRepository memberCourseRepository;
	private final MemberCourseUpdateService memberCourseUpdateService;

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

		System.out.println("[ Scheduled ] 업데이트 대상 개수 : " + memberCourseMap.size());

		memberCourseMap.forEach((member, memberCourseList) -> {
			try {
				memberCourseUpdateService.updateMemberCoursesForMember(member, memberCourseList, objectMapper);
			} catch (Exception e) {
				System.out.println("[ Scheduled ] " + member.getId() + " 사용자 업데이트 실패 " + e.getMessage());
			}
			System.out.println("[ Scheduled ] " + member.getId() + " 사용자 업데이트 성공");
		});
	}

	@Transactional(readOnly = true)
	public MemberCourseListResponse getTodayMemberCourse(Member member) {
		List<MemberCourse> memberCourses = memberCourseRepository
				.findAllByMemberIdAndCourseYearAndCourseSemester(member.getId(), DateHandler.getCurrentSemesterYear(),
						DateHandler.getCurrentSemester());

		// 오늘 날짜 강좌 조회
		CourseDay today = CourseDay.valueOf(getTodayDate());
		List<MemberCourse> todayMemberCourses = new ArrayList<>(memberCourses.stream()
				.filter(memberCourse -> memberCourse.getCourse().getTimetables().stream()
						.anyMatch(timetable -> timetable.getDay().equals(today)))
				.toList());

		// 강좌 시간순 정렬
		todayMemberCourses.sort(Comparator.comparing(mc -> mc.getCourse().getTimetables().stream()
				.filter(timetable -> timetable.getDay().equals(today))
				.map(Timetable::getStartAt)
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
