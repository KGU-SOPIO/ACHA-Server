package sopio.acha.domain.fcm.application;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.fcm.application.exception.FcmSendFailedException;
import sopio.acha.domain.fcm.domain.FcmSchedule;
import sopio.acha.domain.fcm.infrastructure.FcmScheduleRepository;
import sopio.acha.domain.member.domain.Member;

@Service
@RequiredArgsConstructor
public class FcmService {
	private final FcmScheduleRepository fcmScheduleRepository;

	@Transactional
	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
	public void sendActivityNotificationToFCM() {
		fcmScheduleRepository.findAllBySendTimeBefore(LocalDateTime.now())
			.forEach(msg -> {
				try {
					sendNotification(msg.getDeviceToken(), msg.getTitle(), msg.getBody());
				} catch (FirebaseMessagingException e) {
					throw new FcmSendFailedException();
				}
			});
	}

	public void saveFcmEvent(Member currentMember, String title, String body, LocalDateTime sendTime) {
		currentMember.getDevices()
			.forEach(
				device -> fcmScheduleRepository.save(new FcmSchedule(title, body, device.getDeviceToken(), sendTime)));
	}

	public void sendNotification(String token, String title, String body) throws FirebaseMessagingException {
		Notification notification = Notification.builder()
			.setTitle(title)
			.setBody(body)
			.build();
		Message message = Message.builder()
			.setToken(token)
			.setNotification(notification)
			.build();
		FirebaseMessaging.getInstance().send(message);
	}
}
