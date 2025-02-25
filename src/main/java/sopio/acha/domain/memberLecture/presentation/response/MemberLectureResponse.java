package sopio.acha.domain.memberLecture.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.memberLecture.domain.MemberLecture;

@Builder
public record MemberLectureResponse(
	@Schema(description = "사용자 강의 ID", example = "1", requiredMode = REQUIRED)
	Long id,

	@Schema(description = "강의 제목", example = "운영체제", requiredMode = REQUIRED)
	String title,

	@Schema(description = "교수", example = "이병대", requiredMode = REQUIRED)
	String professor,

	@Schema(description = "강의실", example = "3306 강의실", requiredMode = REQUIRED)
	String lectureRoom,

	@Schema(description = "강좌 코드", example = "50742", requiredMode = REQUIRED)
	String code
) {
	public static MemberLectureResponse from(MemberLecture memberLecture) {
		return MemberLectureResponse.builder()
			.id(memberLecture.getLecture().getId())
			.title(memberLecture.getLecture().getTitle())
			.professor(memberLecture.getLecture().getProfessor())
			.lectureRoom(memberLecture.getLecture().getLectureRoom())
			.code(memberLecture.getLecture().getCode())
			.build();
	}
}
