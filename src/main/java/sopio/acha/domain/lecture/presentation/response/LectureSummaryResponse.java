package sopio.acha.domain.lecture.presentation.response;

import lombok.Builder;
import sopio.acha.domain.lecture.domain.Lecture;

@Builder
public record LectureSummaryResponse(
	Long id,
	String name,
	String professor,
	String room
) {
	public static LectureSummaryResponse from(Lecture lecture) {
		return LectureSummaryResponse.builder()
			.id(lecture.getId())
			.name(lecture.getName())
			.professor(lecture.getProfessor())
			.room(lecture.getRoom())
			.build();
	}
}
