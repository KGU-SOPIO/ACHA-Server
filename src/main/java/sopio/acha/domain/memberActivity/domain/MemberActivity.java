package sopio.acha.domain.memberActivity.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.member.domain.Member;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class MemberActivity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "activity_id")
	private Activity activity;

	@Enumerated(STRING)
	private SubmitStatus status;

	public MemberActivity(Member member, Activity activity, String status) {
		this.member = member;
		this.activity = activity;
		this.status = SubmitStatus.valueOf(status);
	}
}
