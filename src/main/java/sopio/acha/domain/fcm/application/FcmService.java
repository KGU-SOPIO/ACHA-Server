package sopio.acha.domain.fcm.application;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.fcm.domain.Device;
import sopio.acha.domain.fcm.infrastructure.DeviceRepository;
import sopio.acha.domain.fcm.presentation.request.AlertRequest;
import sopio.acha.domain.fcm.presentation.response.AlertResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.member.presentation.exception.MemberNotFoundException;

@Service
@RequiredArgsConstructor
public class FcmService {
	private final MemberRepository memberRepository;
	private final DeviceRepository deviceRepository;

	public void setAlertStatus(Member currentMember, AlertRequest alertRequest) {
		currentMember.updateAlert(alertRequest.status());
		memberRepository.save(currentMember);
	}

	public AlertResponse getAlertStatus(Member currentMember) {
		Member member = memberRepository.findMemberById(currentMember.getId())
			.orElseThrow(MemberNotFoundException::new);
		return AlertResponse.of(member.getAlert());
	}

	public void sendNotificationToMember(Member member, String title, String body) {
		if (member.getAlert() == null || !member.getAlert()) {
			return;
		}

		member.getDevices().forEach(device -> {
			try	{
				sendNotification(device.getDeviceToken(), title, body);
			} catch (FirebaseMessagingException e) {
				if (MessagingErrorCode.UNREGISTERED.equals(e.getMessagingErrorCode()) ||
						MessagingErrorCode.INVALID_ARGUMENT.equals(e.getMessagingErrorCode())
				) {
					deviceRepository.deleteByDeviceToken(device.getDeviceToken());
				}
			}
		});
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
