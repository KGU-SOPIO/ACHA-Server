package sopio.acha.domain.activity.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.activity.domain.Activity;

@Builder
public record ActivitySummaryResponse(
	@Schema(description = "강의 제목", example = "컴퓨터 네트워크", requiredMode = REQUIRED)
	String courseName,

	@Schema(description = "접근 여부", example = "true", requiredMode = REQUIRED)
	boolean available,

	@Schema(description = "활동 이름", example = "활동명 예시", requiredMode = REQUIRED)
	String title,

	@Schema(description = "활동 타입", example = "ASSIGNMENT", requiredMode = REQUIRED)
	String type,

	@Schema(description = "활동 코드", example = "50251", requiredMode = REQUIRED)
	String code,

	@Schema(description = "제출 기한 시간", example = "2021-06-30 23:59:59", requiredMode = NOT_REQUIRED)
	String deadline,

	@Schema(description = "활동 링크", example = "https://lms.kyonggi.ac.kr/", requiredMode = NOT_REQUIRED)
	String link,

	@Schema(description = "강의 출석 상태", example = "true", requiredMode = NOT_REQUIRED)
	boolean attendance,

	@Schema(description = "과제 제출 상태", example = "done", requiredMode = NOT_REQUIRED)
	String submitStatus
) {
	public static ActivitySummaryResponse from(Activity activity) {
		return ActivitySummaryResponse.builder()
			.courseName(activity.getLecture().getTitle())
			.available(activity.isAvailable())
			.title(activity.getTitle())
			.type(activity.getType().toString().toLowerCase())
			.code(activity.getCode())
			.deadline(activity.getDeadline().toString())
			.link(activity.getLink().isBlank() ? null : activity.getLink())
			.attendance(activity.isAttendance())
			.submitStatus(activity.getSubmitStatus().toString().toLowerCase())
			.build();
	}
}
