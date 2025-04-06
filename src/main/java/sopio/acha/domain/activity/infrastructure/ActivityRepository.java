package sopio.acha.domain.activity.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.activity.domain.ActivityType;
import sopio.acha.domain.activity.domain.SubmitType;
import sopio.acha.domain.course.domain.Course;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
	boolean existsActivityByTitleAndMemberId(String title, String memberId);

	Optional<Activity> findByTitleAndWeekAndMemberIdAndCourseAndType(String title, int week, String memberId, Course course, ActivityType type);

	@Query("SELECT a FROM Activity a " +
			"WHERE a.member.id = :memberId " +
			"AND a.deadline > :now " +
			"AND ((a.type = :assignmentType AND a.submitStatus = :noneSubmitStatus) " +
			"     OR (a.type = :lectureType AND a.attendance = false)) " +
			"ORDER BY a.deadline ASC")
	List<Activity> findLectureAndAssignmentActivities(
			@Param("memberId") String memberId,
			@Param("now") LocalDateTime now,
			@Param("assignmentType") ActivityType assignmentType,
			@Param("lectureType") ActivityType lectureType,
			@Param("noneSubmitStatus") SubmitType noneSubmitStatus,
			Pageable pageable);

	List<Activity> findAllByMemberIdAndCourseIdOrderByWeekAsc(String memberId, Long CourseId);

	@Query("SELECT a FROM Activity a " +
			"WHERE a.deadline > :now " +
			"AND a.deadline < :end " +
			"AND ((a.type = :assignmentType AND a.submitStatus = :noneSubmitStatus) " +
			"OR (a.type = :lectureType AND a.attendance = false))")
	List<Activity> findAllByDeadlineAfter(
		@Param("now") LocalDateTime now,
		@Param("end") LocalDateTime end,
		@Param("assignmentType") ActivityType assignmentType,
		@Param("lectureType") ActivityType lectureType,
		@Param("noneSubmitStatus") SubmitType noneSubmitStatus);
}
