package sopio.acha.domain.lecture.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.domain.LectureDay;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
	// List<Lecture> findAllByMemberIdAndDayAndIsPresentTrueOrderByStartAtAsc(String memberId, LectureDay day);

	// Boolean existsByMemberId(String memberId);

	Boolean existsByIdentifier(String identifier);

	// List<Lecture> findAllByMemberIdOrderByDayOrderAscStartAtAsc(String memberId);

}
