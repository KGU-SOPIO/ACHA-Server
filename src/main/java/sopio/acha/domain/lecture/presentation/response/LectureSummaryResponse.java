package sopio.acha.domain.lecture.presentation.response;

import lombok.Builder;
import sopio.acha.domain.lecture.domain.Lecture;

@Builder
public record LectureSummaryResponse(
	Long id,
	String title,
	String professor,
	String lectureRoom
) {
	public static LectureSummaryResponse from(Lecture lecture) {
		return LectureSummaryResponse.builder()
			.id(lecture.getId())
			.title(lecture.getTitle())
			.professor(lecture.getProfessor())
			.lectureRoom(lecture.getLectureRoom())
			.build();
	}
}
