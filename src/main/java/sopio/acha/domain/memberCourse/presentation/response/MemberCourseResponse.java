package sopio.acha.domain.memberCourse.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.memberCourse.domain.MemberCourse;
import sopio.acha.domain.timetable.domain.Timetable;

@Builder
public record MemberCourseResponse(
	@Schema(description = "사용자 강좌 ID", example = "1", requiredMode = REQUIRED)
	Long id,

	@Schema(description = "강좌명", example = "운영체제", requiredMode = REQUIRED)
	String title,

	@Schema(description = "교수", example = "이병대", requiredMode = REQUIRED)
	String professor,

	@Schema(description = "강의실", example = "3306 강의실", requiredMode = REQUIRED)
	String lectureRoom,

	@Schema(description = "강좌 코드", example = "50742", requiredMode = REQUIRED)
	String code
) {
	public static MemberCourseResponse from(MemberCourse memberCourse) {
		String lectureRoom = memberCourse.getCourse().getTimetables().stream()
				.findFirst()
				.map(Timetable::getLectureRoom)
				.orElse("이러닝");

		return MemberCourseResponse.builder()
				.id(memberCourse.getCourse().getId())
				.title(memberCourse.getCourse().getTitle())
				.professor(memberCourse.getCourse().getProfessor())
				.lectureRoom(lectureRoom)
				.code(memberCourse.getCourse().getCode())
				.build();
	}
}
