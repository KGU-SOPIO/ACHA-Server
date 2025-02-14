package sopio.acha.domain.memberLecture.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.domain.MemberLecture;

@Repository
public interface MemberLectureRepository extends JpaRepository<MemberLecture, Long> {
	boolean existsByMemberAndLecture(Member member, Lecture lecture);
}
