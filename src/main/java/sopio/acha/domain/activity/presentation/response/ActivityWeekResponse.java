package sopio.acha.domain.activity.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.activity.domain.Activity;

@Builder
public record ActivityWeekResponse(
	@Schema(description = "주차 시작 날짜", example = "2024-09-02", requiredMode = REQUIRED)
	String weekStartAt,

	@Schema(description = "주차 종료 날짜", example = "2024-09-08", requiredMode = REQUIRED)
	String weekEndAt,

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
		LocalDate deadlineDate = activity.getDeadline().toLocalDate();
		LocalDate startOfWeek = deadlineDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate endOfWeek = deadlineDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		return ActivityWeekResponse.builder()
			.weekStartAt(startOfWeek.toString())
			.weekEndAt(endOfWeek.toString())
			.available(activity.isAvailable())
			.week(activity.getWeek())
			.title(activity.getTitle())
			.code(activity.getCode())
			.link(activity.getLink())
			.type(activity.getType().toString().toLowerCase())
			.build();
	}
}
