package sopio.acha.domain.activity.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.activity.domain.Activity;

@Builder
public record ActivitySummaryResponse(
	@Schema(description = "강의 제목", example = "컴퓨터 네트워크", requiredMode = REQUIRED)
	String lectureTitle,

	@Schema(description = "활동 이름", example = "활동명 예시", requiredMode = REQUIRED)
	String title,

	@Schema(description = "활동 타입", example = "ASSIGNMENT", requiredMode = REQUIRED)
	String activityType,

	@Schema(description = "활동 코드", example = "50251", requiredMode = REQUIRED)
	String code,

	@Schema(description = "제출 기한 날짜", example = "2021-06-30", requiredMode = NOT_REQUIRED)
	String deadlineDay,

	@Schema(description = "제출 기한 시간", example = "23:59:59", requiredMode = NOT_REQUIRED)
	String deadlineTime,

	@Schema(description = "활동 링크", example = "https://lms.kyonggi.ac.kr/", requiredMode = NOT_REQUIRED)
	String link
) {
	public static ActivitySummaryResponse from(Activity activity) {
		return ActivitySummaryResponse.builder()
			.lectureTitle(activity.getLecture().getTitle())
			.title(activity.getTitle())
			.activityType(activity.getType().toString())
			.code(activity.getCode())
			.deadlineDay(activity.getDeadline().toLocalDate().toString())
			.deadlineTime(activity.getDeadline().toLocalTime().toString())
			.link(activity.getLink())
			.build();
	}
}
