package sopio.acha.domain.lecture.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LectureBasicInformationResponse (
	@Schema(description = "강좌 이름", requiredMode = REQUIRED)
	String title,

	@Schema(description = "강좌 식별 번호", requiredMode = REQUIRED)
	String identifier,

	@Schema(description = "담당 교수", requiredMode = REQUIRED)
	String professor
) {
}
