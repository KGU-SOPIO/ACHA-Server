package sopio.acha.domain.lecture.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import java.util.List;

import org.hibernate.annotations.Formula;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.notification.domain.Notification;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class Lecture extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, unique = true)
	private String identifier;

	@Column(nullable = false)
	private String code;

	@Column(nullable = false)
	private String professor;

	private String lectureRoom;

	@Enumerated(STRING)
	private LectureDay day;

	@Column(nullable = false)
	private String year;

	@Column(nullable = false)
	private String semester;

	@Formula("CASE WHEN day = '월요일' THEN 1 " +
		"WHEN day = '화요일' THEN 2 " +
		"WHEN day = '수요일' THEN 3 " +
		"WHEN day = '목요일' THEN 4 " +
		"WHEN day = '금요일' THEN 5 " +
		"ELSE 0 END")
	private int dayOrder;

	private int classTime;

	private int startAt;

	private int endAt;

	@OneToMany(mappedBy = "notification", cascade = ALL, fetch = LAZY)
	private List<Notification> notifications;

	public static Lecture save(String title, String identifier, String code, String professor) {
		return Lecture.builder()
			.title(title)
			.identifier(identifier)
			.code(code)
			.professor(professor)
			.year(DateHandler.getCurrentSemesterYear())
			.semester(DateHandler.getCurrentSemester())
			.build();
	}

	public void setTimeTable(String day, int classTime, int startAt, int endAt, String lectureRoom) {
		this.day = LectureDay.valueOf(day);
		this.classTime = classTime;
		this.startAt = startAt;
		this.endAt = endAt;
		this.lectureRoom = lectureRoom;
	}
}
