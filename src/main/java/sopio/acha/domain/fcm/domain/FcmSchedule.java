package sopio.acha.domain.fcm.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class FcmSchedule extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	private String title;

	private String body;

	private String deviceToken;

	private LocalDateTime sendTime;

	public FcmSchedule(String title, String body, String deviceToken, LocalDateTime sendTime) {
		this.title = title;
		this.body = body;
		this.deviceToken = deviceToken;
		this.sendTime = sendTime;
	}
}
