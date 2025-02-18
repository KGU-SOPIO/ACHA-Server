package sopio.acha.domain.memberLecture.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.domain.LectureDay;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.domain.MemberLecture;

@Repository
public interface MemberLectureRepository extends JpaRepository<MemberLecture, Long> {
	boolean existsByMemberAndLecture(Member member, Lecture lecture);

	List<MemberLecture> findAllByMemberIdAndLectureDayAndLectureYearAndLectureSemester(String memberId,
		LectureDay lectureDay, String year, String semester);

	List<MemberLecture> findAllByMemberIdAndLectureYearAndLectureSemesterOrderByLectureDayOrderAsc(String memberId, String year, String semester);
}
