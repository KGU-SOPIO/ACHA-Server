package sopio.acha.domain.memberCourse.application;

import static sopio.acha.common.handler.DateHandler.getTodayDate;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.domain.CourseDay;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberCourse.domain.MemberCourse;
import sopio.acha.domain.memberCourse.infrastructure.MemberCourseRepository;
import sopio.acha.domain.memberCourse.presentation.response.MemberCourseListResponse;

@Service
@RequiredArgsConstructor
public class MemberCourseService {
	private final MemberCourseRepository memberCourseRepository;

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
		return MemberCourseListResponse.from(memberCourses);
	}

	@Transactional(readOnly = true)
	public MemberCourseListResponse getThisSemesterMemberCourse(Member currentMember) {
		List<MemberCourse> memberCourses = memberCourseRepository.findAllByMemberIdAndCourseYearAndCourseSemesterOrderByCourseDayOrderAsc(
			currentMember.getId(), DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester());
		return MemberCourseListResponse.from(memberCourses);
	}

	@Transactional
	public List<MemberCourse> getAllMemberCourse() {
		return memberCourseRepository.findAllByCourseYearAndCourseSemesterOrderByCourseDayOrderAsc(
			DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester());
	}

	public boolean isExistsMemberCourse(Member currentMember, Course course) {
		return !memberCourseRepository.existsByMemberAndCourse(currentMember, course);
	}
}
