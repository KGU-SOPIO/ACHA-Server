package sopio.acha.domain.memberLecture.domain;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.time.LocalDateTime.now;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.member.domain.Member;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class MemberLecture extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "lecture_id")
	private Lecture lecture;

	public MemberLecture(Member member, Lecture lecture) {
		this.member = member;
		this.lecture = lecture;
	}

	private static final long BASE_HOUR = 1;

	public boolean checkLastUpdatedAt() {
		return updatedAt.plusHours(BASE_HOUR).isBefore(now());
	}
}
