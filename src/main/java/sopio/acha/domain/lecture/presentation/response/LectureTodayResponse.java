package sopio.acha.domain.lecture.presentation.response;

import lombok.Builder;
import sopio.acha.domain.lecture.domain.Lecture;

@Builder
public record LectureTodayResponse(
	Long id,
	String title,
	String professor,
	String lectureRoom
) {
	public static LectureTodayResponse from(Lecture lecture) {
		return LectureTodayResponse.builder()
			.id(lecture.getId())
			.title(lecture.getTitle())
			.professor(lecture.getProfessor())
			.lectureRoom(lecture.getLectureRoom())
			.build();
	}
}
