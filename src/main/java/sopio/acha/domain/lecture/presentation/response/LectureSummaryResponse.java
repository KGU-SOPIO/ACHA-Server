package sopio.acha.domain.lecture.presentation.response;

import lombok.Builder;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.domain.LectureDay;

@Builder
public record LectureSummaryResponse(
	Long id,
	String title,
	String professor,
	String lectureRoom,
	String classTime
) {
	public static LectureSummaryResponse from(Lecture lecture) {
		StringBuilder classTimeBuilder = new StringBuilder();
		classTimeBuilder.append(String.valueOf(lecture.getDay()).charAt(0));
		for (int i = lecture.getStartAt(); i <= lecture.getEndAt(); i++) {
			classTimeBuilder.append(i);
		}
		return LectureSummaryResponse.builder()
			.id(lecture.getId())
			.title(lecture.getTitle())
			.professor(lecture.getProfessor())
			.lectureRoom(lecture.getLectureRoom())
			.classTime(classTimeBuilder.toString())
			.build();
	}
}
