package sopio.acha.domain.notification.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.notification.application.response.NotificationScrapingResponse;
import sopio.acha.domain.notification.domain.Notification;
import sopio.acha.domain.notification.infrastructure.NotificationRepository;

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

	private boolean isExistsByIndexAndLectureId(int index, Long lectureId) {
		return notificationRepository.existsByIndexAndLectureId(index, lectureId);
	}
}
