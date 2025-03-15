package sopio.acha.domain.fcm.domain;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.domain.member.domain.Member;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Device extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Column(unique = true)
	private String deviceToken;

	public Device(Member member, String deviceToken) {
		this.member = member;
		this.deviceToken = deviceToken;
	}

	public void updateDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
}
