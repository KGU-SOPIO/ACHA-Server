package sopio.acha.domain.activity.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityResponse(
	@Schema(description = "접근 여부", example = "true", requiredMode = REQUIRED)
	boolean available,

	@Schema(description = "주차", example = "1", requiredMode = REQUIRED)
	@Min(1) @Max(16)
	int week,

	@Schema(description = "활동 이름", example = "활동명 예시", requiredMode = REQUIRED)
	String title,

	@Schema(description = "활동 링크", example = "https://lms.kyonggi.ac.kr/", requiredMode = NOT_REQUIRED)
	String link,

	@Schema(description = "활동 타입", example = "assignment", requiredMode = REQUIRED)
	String type,

	@Schema(description = "활동 코드", example = "50251", requiredMode = NOT_REQUIRED)
	String code,

	@Schema(description = "강좌 수강 시작 시각", example = "2021-06-30 00:00:00", requiredMode = NOT_REQUIRED)
	String startAt,

	@Schema(description = "강좌 수강 시간", example = "00:00:00", requiredMode = NOT_REQUIRED)
	String courseTime,

	@Schema(description = "제출 기한", example = "2021-06-30 23:59:59", requiredMode = NOT_REQUIRED)
	String deadline,

	@Schema(description = "남은 시간", example = "제출 마감이 지난 시간: 00 일 01 시간", requiredMode = NOT_REQUIRED)
	String timeLeft,

	@Schema(description = "활동 설명", example = "활동 설명 예시", requiredMode = NOT_REQUIRED)
	String description,

	@Schema(description = "강의 출석 상태", example = "true", requiredMode = NOT_REQUIRED)
	boolean attendance,

	@Schema(description = "과제 제출 상태", example = "done", requiredMode = NOT_REQUIRED)
	String submitStatus
) {}
