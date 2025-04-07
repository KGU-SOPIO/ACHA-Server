package sopio.acha.domain.activity.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityScrapingWeekResponse(
        @Schema(description = "주차", example = "1", requiredMode = REQUIRED) int week,

        @Schema(description = "활동 목록", requiredMode = REQUIRED) List<ActivityScrapingResponse> activities) {
}
