package sopio.acha.domain.fcm.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.domain.member.domain.Member;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private LocalDateTime sendTime;

	public FcmSchedule(String title, String body, String deviceToken, Member member, LocalDateTime sendTime) {
		this.title = title;
		this.body = body;
		this.deviceToken = deviceToken;
		this.member = member;
		this.sendTime = sendTime;
	}
}
