package sopio.acha.domain.notification.domain;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.domain.lecture.domain.Lecture;

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
	@JoinColumn(name = "lecture_id")
	private Lecture lecture;

	public static Notification save(int index, String title, String date, String content, String link,
		Lecture lecture) {
		return Notification.builder()
			.index(index)
			.title(title)
			.date(date)
			.content(content)
			.link(link)
			.lecture(lecture)
			.build();
	}
}
