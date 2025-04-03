package sopio.acha.domain.notification.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.notification.domain.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	boolean existsByIndexAndCourseId(int index, Long lectureId);

	Optional<Notification> findByIndexAndCourseId(int index, Long lectureId);

	List<Notification> findAllByCourseCodeOrderByIndexDesc(String code);

	List<Notification> findAllByCourseId(Long courseId);
}
