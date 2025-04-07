package sopio.acha.domain.course.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.course.domain.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
	Optional<Course> findByIdentifier(String identifier);

	Optional<Course> findByCode(String code);

	List<Course> findAllByIdentifierIn(Collection<String> identifiers);
}
