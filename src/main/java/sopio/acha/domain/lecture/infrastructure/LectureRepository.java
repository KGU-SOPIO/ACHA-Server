package sopio.acha.domain.lecture.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.lecture.domain.Lecture;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
	List<Lecture> findAllByMemberIdAndDayAndIsPresentTrue(String memberId, String day);
	Boolean existsByMemberId(String memberId);
}
