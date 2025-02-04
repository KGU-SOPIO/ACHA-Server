package sopio.acha.domain.lecture.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	private String title;

	private String identifier;

	private String professor;

	private String lectureRoom;

	private String day;

	private int classTime;

	private int startAt;

	private int endAt;

	private Boolean isPresent = true;

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;

	public static List<Lecture> convert(List<Object> lectureList, Member currentMember) {
		return lectureList.stream()
			.map(item -> {
				if (item instanceof Map<?, ?> map) {
					Lecture lecture = new Lecture();
					lecture.title = (String) map.get("title");
					lecture.identifier = (String) map.get("identifier");
					lecture.professor = (String) map.get("professor");
					lecture.lectureRoom = (String) map.get("lectureRoom");
					lecture.day = (String) map.get("day");
					lecture.classTime = Optional.ofNullable((Integer) map.get("classTime")).orElse(0);
					lecture.startAt = Optional.ofNullable((Integer) map.get("startAt")).orElse(0);
					lecture.endAt = Optional.ofNullable((Integer) map.get("endAt")).orElse(0);
					lecture.member = currentMember;
					return lecture;
				}
				throw new ExtractorErrorException();
			})
			.toList();
	}
}
