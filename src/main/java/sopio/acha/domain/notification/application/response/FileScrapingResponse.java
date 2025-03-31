package sopio.acha.domain.notification.application.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FileScrapingResponse(
    @Schema(description = "파일 제목", requiredMode = REQUIRED)
    String title,

    @Schema(description = "파일 링크", requiredMode = REQUIRED)
    String link
) {}
