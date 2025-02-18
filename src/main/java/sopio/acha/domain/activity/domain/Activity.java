package sopio.acha.domain.activity.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static sopio.acha.domain.activity.domain.ActivityType.ASSIGNMENT;
import static sopio.acha.domain.activity.domain.ActivityType.LECTURE;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.domain.activity.presentation.exception.InvalidActivityTypeException;
import sopio.acha.domain.lecture.domain.Lecture;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class Activity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(nullable = false)
	private boolean available;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 499)
	private String link;

	@Enumerated(STRING)
	private ActivityType type;

	@Column(nullable = false)
	private String code;

	private LocalDateTime startAt;

	private LocalDateTime deadline;

	private String lectureTime;

	private String timeLeft;

	private String description;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "lecture_id")
	private Lecture lecture;

	public static Activity saveAssignment(boolean available, String title, String link, String type, String code,
		String deadline, String timeLeft, String description) {
		LocalDateTime convertedDeadline = LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		if (!Objects.equals(type, ASSIGNMENT.toString())) throw new InvalidActivityTypeException();
		return Activity.builder()
			.available(available)
			.title(title)
			.link(link)
			.type(ASSIGNMENT)
			.code(code)
			.deadline(convertedDeadline)
			.timeLeft(timeLeft)
			.description(description)
			.build();
	}

	public static Activity saveLecture(boolean available, String title, String link, String type, String code,
		String startAt, String deadline, String lectureTime) {
		LocalDateTime convertedStartAt = LocalDateTime.parse(startAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime convertedDeadline = LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		if (!Objects.equals(type, LECTURE.toString())) throw new InvalidActivityTypeException();
		return Activity.builder()
			.available(available)
			.title(title)
			.link(link)
			.type(LECTURE)
			.code(code)
			.startAt(convertedStartAt)
			.deadline(convertedDeadline)
			.lectureTime(lectureTime)
			.build();
	}
}
