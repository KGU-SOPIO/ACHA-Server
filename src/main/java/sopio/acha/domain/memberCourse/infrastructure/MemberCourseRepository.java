package sopio.acha.domain.memberCourse.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.memberCourse.domain.MemberCourse;

@Repository
public interface MemberCourseRepository extends JpaRepository<MemberCourse, Long> {
	boolean existsByMemberIdAndCourseId(String memberId, Long courseId);

	List<MemberCourse> findAllByMemberIdAndCourseYearAndCourseSemester(String memberId, String year, String semester);

	List<MemberCourse> findTop70ByCourseYearAndCourseSemesterOrderByUpdatedAtAsc(String year, String semester);
}
