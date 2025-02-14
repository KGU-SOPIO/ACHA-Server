package sopio.acha.domain.lecture.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LectureTimeTableResponse(
	@Schema(description = "강좌 식별 번호", example = "1493", requiredMode = REQUIRED)
	String identifier,

	@Schema(description = "강의 요일", example = "화요일", requiredMode = REQUIRED)
	String day,

	@Schema(description = "강의 시간", example = "3", requiredMode = REQUIRED)
	int classTime,

	@Schema(description = "강의 시작 교시", example = "1", requiredMode = REQUIRED)
	int startAt,

	@Schema(description = "강의 종료 교시", example = "3", requiredMode = REQUIRED)
	int endAt,

	@Schema(description = "강의 교실", example = "3306", requiredMode = REQUIRED)
	String lectureRoom
) {
}
