package sopio.acha.domain.notification.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.notification.domain.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	boolean existsByIndexAndLectureId(int index, Long lectureId);

	List<Notification> findAllByLectureCode(String code);
}
