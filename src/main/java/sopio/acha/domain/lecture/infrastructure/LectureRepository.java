package sopio.acha.domain.lecture.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.lecture.domain.Lecture;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

	boolean existsByIdentifier(String identifier);

	Optional<Lecture> findByIdentifier(String identifier);

	Optional<Lecture> findByCode(String code);
}
