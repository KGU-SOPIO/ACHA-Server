package sopio.acha.domain.lecture.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.lecture.domain.Lecture;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

	boolean existsByIdentifier(String identifier);
}
