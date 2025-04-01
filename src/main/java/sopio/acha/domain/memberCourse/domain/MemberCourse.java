package sopio.acha.domain.memberCourse.domain;

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
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.member.domain.Member;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class MemberCourse extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "course_id")
	private Course course;

	public MemberCourse(Member member, Course course) {
		this.member = member;
		this.course = course;
	}

	private static final long BASE_HOUR = 1;

	public boolean checkLastUpdatedAt() {
		return updatedAt.plusHours(BASE_HOUR).isBefore(now());
	}
}
