package sopio.acha.domain.notification.application;

import java.util.List;
import java.util.Optional;

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

	/// 공지사항 데이터를 조회하여 존재하면 업데이트하고, 존재하지 않으면 새로 생성합니다.
	/// title, link, courseId를 기준으로 공지사항을 조회합니다.
	@Transactional
	public void saveOrUpdateNotification(List<NotificationScrapingResponse> notifications, Course course) {
		notifications.forEach(notificationObject -> {
			Optional<Notification> optionalNotification = notificationRepository
					.findByTitleAndLinkAndCourseId(notificationObject.title(), notificationObject.link(),
							course.getId());

			int index = Integer.parseInt(notificationObject.index());
			String title = notificationObject.title();
			String date = notificationObject.date();
			String content = notificationObject.content();
			String link = notificationObject.link();

			if (optionalNotification.isPresent()) {
				optionalNotification.get().update(index, title, date, content, link);
			} else {
				Notification newNotification = Notification.save(index, title, date, content, link, course);
				notificationRepository.save(newNotification);
			}
		});
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
}
