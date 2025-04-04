package sopio.acha.domain.notification.domain;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.domain.course.domain.Course;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class Notification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "`index`")
	private int index;

	private String title;

	private String date;

	@Column(columnDefinition = "TEXT")
	private String content;

	private String link;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "course_id")
	private Course course;

	@Builder.Default
	@OneToMany(fetch = LAZY, orphanRemoval = true, mappedBy = "notification")
	private List<NotificationFile> notificationFiles = new ArrayList<>();

	public static Notification save(int index, String title, String date, String content, String link,
		Course course) {
		return Notification.builder()
			.index(index)
			.title(title)
			.date(date)
			.content(content)
			.link(link)
			.course(course)
			.build();
	}

	public void update(int index, String title, String date, String content, String link) {
		this.index = index;
		this.title = title;
		this.date = date;
		this.content = content;
		this.link = link;
	}
}
