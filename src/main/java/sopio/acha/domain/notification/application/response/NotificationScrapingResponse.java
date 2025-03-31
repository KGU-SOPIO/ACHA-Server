package sopio.acha.domain.notification.application.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationScrapingResponse(
	@Schema(description = "공지사항 링크", requiredMode = REQUIRED)
	String link,

	@Schema(description = "공지사항 내용", example = "안녕하세요 여러분 홍길동 교수입니다", requiredMode = REQUIRED)
	String content,

	@Schema(description = "공지사항 인덱스", example = "1", requiredMode = REQUIRED)
	String index,

	@Schema(description = "공지사항 제목", example = "공지사항 제목 예시", requiredMode = REQUIRED)
	String title,

	@Schema(description = "공지사항 날짜", example = "2021-09-01", requiredMode = REQUIRED)
	String date,

	@Schema(description = "공지사항 파일 목록", requiredMode = NOT_REQUIRED)
	List<FileScrapingResponse> files
) {}
