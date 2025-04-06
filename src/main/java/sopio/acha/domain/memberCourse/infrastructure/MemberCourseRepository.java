package sopio.acha.domain.memberCourse.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.domain.CourseDay;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberCourse.domain.MemberCourse;

@Repository
public interface MemberCourseRepository extends JpaRepository<MemberCourse, Long> {
	boolean existsByMemberAndCourse(Member member, Course course);

	List<MemberCourse> findAllByMemberIdAndCourseDayAndCourseYearAndCourseSemester(String memberId,
																					  CourseDay courseDay, String year, String semester);

	List<MemberCourse> findAllByMemberIdAndCourseYearAndCourseSemester(String memberId, String year, String semester);

	List<MemberCourse> findAllByCourseYearAndCourseSemesterOrderByCourseDayOrderAsc(String year, String semester);
}
