package sopio.acha.domain.notification.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.notification.application.response.NotificationScrapingResponse;
import sopio.acha.domain.notification.domain.Notification;
import sopio.acha.domain.notification.infrastructure.NotificationRepository;
import sopio.acha.domain.notification.presentation.exception.NotificationNotFoundException;
import sopio.acha.domain.notification.presentation.response.NotificationDetailResponse;
import sopio.acha.domain.notification.presentation.response.NotificationListResponse;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;

	@Transactional
	public void extractNotifications(List<NotificationScrapingResponse> notifications, Course course) {
		List<Notification> existingNotifications = notificationRepository.findAllByCourseId(course.getId());
		if (!existingNotifications.isEmpty()) {
			notificationRepository.deleteAll(existingNotifications);
		}

		List<Notification> newNotifications = notifications.stream()
						.map(n -> Notification.save(
								Integer.parseInt(n.index()),
								n.title(),
								n.date(),
								n.content(),
								n.link(),
								course
						))
						.toList();
		notificationRepository.saveAll(newNotifications);
	}

	@Transactional(readOnly = true)
	public NotificationListResponse getNotifications(String code) {
		List<Notification> notifications = notificationRepository.findAllByCourseCodeOrderByIndexDesc(code);
		if (notifications.isEmpty())
			throw new NotificationNotFoundException();
		return NotificationListResponse.from(notifications.getFirst().getCourse().getTitle(), notifications);
	}

	@Transactional(readOnly = true)
	public NotificationDetailResponse getNotificationDetail(Long notificationId) {
		Notification currentNotification = notificationRepository.findById(notificationId)
			.orElseThrow(NotificationNotFoundException::new);
		Notification prevNotification = notificationRepository.findByIndexAndCourseId(
			currentNotification.getIndex() - 1, currentNotification.getCourse().getId())
			.orElse(null);
		Notification nextNotification = notificationRepository.findByIndexAndCourseId(
			currentNotification.getIndex() + 1, currentNotification.getCourse().getId())
			.orElse(null);
		return NotificationDetailResponse.of(currentNotification, prevNotification, nextNotification);
	}

	private boolean isExistsByIndexAndCourseId(int index, Long courseId) {
		return notificationRepository.existsByIndexAndCourseId(index, courseId);
	}
}
