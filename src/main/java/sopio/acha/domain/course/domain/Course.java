package sopio.acha.domain.course.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.notification.domain.Notification;
import sopio.acha.domain.timetable.domain.Timetable;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class Course extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, unique = true)
	private String identifier;

	@Column(nullable = false)
	private String code;

	private String noticeBoardCode;

	@Column(nullable = false)
	private String professor;

	@Column(nullable = false)
	private String year;

	@Column(nullable = false)
	private String semester;

	@Builder.Default
	@OneToMany(mappedBy = "course", cascade = ALL, fetch = LAZY)
	private List<Notification> notifications = new ArrayList<>();

	@Getter
	@Builder.Default
	@OneToMany(mappedBy = "course", cascade = ALL, fetch = LAZY)
	private List<Timetable> timetables = new ArrayList<>();

	public static Course save(String title, String identifier, String code, String noticeCode, String professor) {
		return Course.builder()
				.title(title)
				.identifier(identifier)
				.code(code)
				.noticeBoardCode(noticeCode)
				.professor((professor == null || professor.trim().isEmpty()) ? "이러닝" : professor)
				.year(DateHandler.getCurrentSemesterYear())
				.semester(DateHandler.getCurrentSemester())
				.build();
	}

	public void addTimetable(String day, int classTime, int startAt, int endAt, String lectureRoom) {
		Timetable timetable = Timetable.builder()
				.day(CourseDay.valueOf(day))
				.classTime(classTime)
				.startAt(startAt)
				.endAt(endAt)
				.lectureRoom(lectureRoom)
				.build();
		timetable.updateCourse(this);
		this.timetables.add(timetable);
	}
}
