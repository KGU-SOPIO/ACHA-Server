package sopio.acha.domain.lecture.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.List;
import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.common.exception.ExtractorErrorException;
import sopio.acha.domain.member.domain.Member;

@Entity
@Table(name = "lecture")
@Getter
public class Lecture extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	private String name;

	private String identifier;

	private String professor;

	private String room;

	private String day;

	private int time;

	private int startAt;

	private int endAt;

	private Boolean isPresent = true;

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;

	public static List<Lecture> convert(List<Object> lectureList) {
		return lectureList.stream()
			.map(item -> {
				if (item instanceof Map<?, ?> map) {
					Lecture lecture = new Lecture();
					lecture.name = (String) map.get("courseName");
					lecture.identifier = (String) map.get("courseIdentifier");
					lecture.professor = (String) map.get("professor");
					lecture.room = (String) map.get("lectureRoom");
					lecture.day = (String) map.get("day");
					lecture.time = (int) map.get("classTime");
					lecture.startAt = (int) map.get("startAt");
					lecture.endAt = (int) map.get("endAt");
					return lecture;
				}
				throw new ExtractorErrorException();
			})
			.toList();
	}
}
