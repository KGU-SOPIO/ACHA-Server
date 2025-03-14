package sopio.acha.domain.notification.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.lecture.domain.Lecture;
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

	public void extractNotifications(List<NotificationScrapingResponse> notifications, Lecture lecture) {
		notificationRepository.saveAll(notifications.stream()
			.filter(n -> !isExistsByIndexAndLectureId(Integer.parseInt(n.index()), lecture.getId()))
			.map(n -> Notification.save(Integer.parseInt(n.index()), n.title(), n.date(),
				n.content(), n.link(), lecture))
			.collect(Collectors.toList()));
	}

	@Transactional(readOnly = true)
	public NotificationListResponse getNotifications(String code) {
		List<Notification> notifications = notificationRepository.findAllByLectureCodeOrderByIndexDesc(code);
		if (notifications.isEmpty())
			throw new NotificationNotFoundException();
		return NotificationListResponse.from(notifications.getFirst().getLecture().getTitle(), notifications);
	}

	@Transactional(readOnly = true)
	public NotificationDetailResponse getNotificationDetail(Long notificationId) {
		Notification currentNotification = notificationRepository.findById(notificationId)
			.orElseThrow(NotificationNotFoundException::new);
		Notification prevNotification = notificationRepository.findByIndexAndLectureId(
			currentNotification.getIndex() - 1, currentNotification.getLecture().getId())
			.orElse(null);
		Notification nextNotification = notificationRepository.findByIndexAndLectureId(
			currentNotification.getIndex() + 1, currentNotification.getLecture().getId())
			.orElse(null);
		return NotificationDetailResponse.of(currentNotification, prevNotification, nextNotification);
	}

	private boolean isExistsByIndexAndLectureId(int index, Long lectureId) {
		return notificationRepository.existsByIndexAndLectureId(index, lectureId);
	}
}
