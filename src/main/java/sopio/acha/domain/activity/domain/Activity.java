package sopio.acha.domain.activity.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static sopio.acha.domain.activity.domain.ActivityType.ASSIGNMENT;
import static sopio.acha.domain.activity.domain.ActivityType.ETC;
import static sopio.acha.domain.activity.domain.ActivityType.FILE;
import static sopio.acha.domain.activity.domain.ActivityType.LECTURE;
import static sopio.acha.domain.activity.domain.ActivityType.URL;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.member.domain.Member;

@Getter
@Entity
@Builder
@Table(indexes = {
		@Index(name = "idx_activity_course_id", columnList = "course_id"),
		@Index(name = "idx_activity_member_id", columnList = "member_id"),
		@Index(name = "idx_activity_week", columnList = "week")
})
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class Activity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(nullable = false)
	private boolean available;

	@Column(nullable = false)
	private int week;

	@Column(nullable = false)
	private String title;

	@Column(length = 499)
	private String link;

	@Enumerated(STRING)
	private ActivityType type;

	private String code;

	private LocalDateTime startAt;

	private LocalDateTime deadline;

	private String courseTime;

	private String timeLeft;

	private boolean attendance;

	@Enumerated(STRING)
	private SubmitType submitStatus;

	@Column(columnDefinition = "TEXT")
	private String description;

	private boolean notifiedThreeDays;
	private boolean notifiedOneDay;
	private boolean notifiedOneHour;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "course_id")
	private Course course;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public void updateNotifiedThreeDays(boolean notifiedThreeDays) {
		this.notifiedThreeDays = notifiedThreeDays;
	}

	public void updateNotifiedOneDay(boolean notifiedOneDay) {
		this.notifiedOneDay = notifiedOneDay;
	}

	public void updateNotifiedOneHour(boolean notifiedOneHour) {
		this.notifiedOneHour = notifiedOneHour;
	}

	public static Activity save(boolean available, int week, String title, String link, String type, String code,
			String deadline,
			String startAt, String lectureTime, String timeLeft, String description, boolean attendance,
			String submitStatus, Course course, Member member) {
		LocalDateTime convertedStartAt = null;
		if (startAt != null)
			convertedStartAt = DateHandler.parseDateTime(startAt);
		LocalDateTime convertedDeadline = null;
		if (deadline != null)
			convertedDeadline = DateHandler.parseDateTime(deadline);
		return switch (type) {
			case "assignment" -> Activity.builder()
					.available(available)
					.title(title)
					.week(week)
					.link(link)
					.type(ASSIGNMENT)
					.code(code)
					.deadline(convertedDeadline)
					.timeLeft(timeLeft)
					.description(description)
					.submitStatus(SubmitType.fromString(submitStatus))
					.course(course)
					.member(member)
					.build();
			case "lecture" -> Activity.builder()
					.available(available)
					.title(title)
					.week(week)
					.link(link)
					.type(LECTURE)
					.code(code)
					.startAt(convertedStartAt)
					.deadline(convertedDeadline)
					.courseTime(lectureTime)
					.attendance(attendance)
					.course(course)
					.member(member)
					.build();
			case "url" -> Activity.builder()
					.available(available)
					.title(title)
					.week(week)
					.link(link)
					.type(URL)
					.code(code)
					.course(course)
					.member(member)
					.build();
			case "file" -> Activity.builder()
					.available(available)
					.title(title)
					.week(week)
					.code(code)
					.link(link)
					.type(FILE)
					.course(course)
					.member(member)
					.build();
			default -> Activity.builder()
					.available(available)
					.title(title)
					.week(week)
					.code(code)
					.type(ETC)
					.course(course)
					.member(member)
					.build();
		};
	}

	public void update(boolean available, String link, String deadline, String timeLeft, String description,
			boolean attendance, String submitStatus) {
		this.available = available;
		if (available) {
			this.link = link;
			this.deadline = DateHandler.parseDateTime(deadline);
			this.timeLeft = timeLeft;
			this.description = description;
			this.attendance = attendance;
			this.submitStatus = SubmitType.fromString(submitStatus);
		}
	}
}
