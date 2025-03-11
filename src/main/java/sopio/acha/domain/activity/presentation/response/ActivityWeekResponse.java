package sopio.acha.domain.activity.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.activity.domain.Activity;

@Builder
public record ActivityWeekResponse(
	@Schema(description = "접근 여부", example = "true", requiredMode = REQUIRED)
	boolean available,

	@Schema(description = "주차", example = "2", requiredMode = REQUIRED)
	int week,

	@Schema(description = "활동 이름", example = "활동명 예시", requiredMode = REQUIRED)
	String title,

	@Schema(description = "활동 코드", example = "50251", requiredMode = NOT_REQUIRED)
	String code,

	@Schema(description = "활동 링크", example = "https://lms.kyonggi.ac.kr/", requiredMode = NOT_REQUIRED)
	String link,

	@Schema(description = "활동 타입", example = "ASSIGNMENT", requiredMode = REQUIRED)
	String type
) {
	public static ActivityWeekResponse from(Activity activity) {
		return ActivityWeekResponse.builder()
			.available(activity.isAvailable())
			.week(activity.getWeek())
			.title(activity.getTitle())
			.code(activity.getCode())
			.link(activity.getLink())
			.type(activity.getType().toString().toLowerCase())
			.build();
	}
}
