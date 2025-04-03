package sopio.acha.domain.fcm.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.fcm.domain.Device;
import sopio.acha.domain.fcm.domain.FcmSchedule;
import sopio.acha.domain.fcm.infrastructure.DeviceRepository;
import sopio.acha.domain.fcm.infrastructure.FcmScheduleRepository;
import sopio.acha.domain.fcm.presentation.request.AlertRequest;
import sopio.acha.domain.fcm.presentation.response.AlertResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.member.presentation.exception.MemberNotFoundException;

@Service
@RequiredArgsConstructor
public class FcmService {
	private final FcmScheduleRepository fcmScheduleRepository;
	private final MemberRepository memberRepository;
	private final DeviceRepository deviceRepository;

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
	public void sendActivityNotificationToFCM() {
		List<FcmSchedule> fcmSchedule = fcmScheduleRepository.findAllBySendTimeBeforeAndMember_AlertIsTrue(LocalDateTime.now());
		for (FcmSchedule schedule : fcmSchedule) {
			try {
				sendNotification(schedule.getDeviceToken(), schedule.getTitle(), schedule.getBody());
			} catch (FirebaseMessagingException e) {
				if ("messaging/registration-token-not-registered".equals(e.getErrorCode().toString()) ||
						"messaging/invalid-registration-token".equals(e.getErrorCode().toString()) ||
						"messaging/invalid-argument".equals(e.getErrorCode().toString())) {
					deviceRepository.deleteByDeviceToken(schedule.getDeviceToken());
				}
			}
		}
	}

	public void setAlertStatus(Member currentMember, AlertRequest alertRequest) {
		currentMember.updateAlert(alertRequest.status());
		memberRepository.save(currentMember);
	}

	public AlertResponse getAlertStatus(Member currentMember) {
		Member member = memberRepository.findMemberById(currentMember.getId())
			.orElseThrow(MemberNotFoundException::new);
		return AlertResponse.of(member.getAlert());
	}

	public void saveFcmEvent(Member currentMember, String title, String body, LocalDateTime sendTime) {
		currentMember.getDevices()
			.forEach(
				device -> fcmScheduleRepository.save(new FcmSchedule(title, body, device.getDeviceToken(), currentMember, sendTime)));
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

	public void addFcmToken(Member currentMember, String token) {
		deviceRepository.save(new Device(currentMember, token));
	}
}
