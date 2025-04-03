package sopio.acha.domain.course.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingWeekResponse;
import sopio.acha.domain.notification.application.response.NotificationScrapingResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CourseBasicInformationResponse(
	@Schema(description = "강좌 이름", requiredMode = REQUIRED)
	String title,

	@Schema(description = "강좌 식별 번호", requiredMode = REQUIRED)
	String identifier,

	@Schema(description = "강좌 코드", requiredMode = REQUIRED)
	String code,

	@Schema(description = "담당 교수", requiredMode = REQUIRED)
	String professor,

	@Schema(description = "공지사항 페이지 코드", requiredMode = NOT_REQUIRED)
	String noticeCode,

	@Schema(description = "공지사항 목록", requiredMode = NOT_REQUIRED)
	List<NotificationScrapingResponse> notices,

	@Schema(description = "활동 목록", requiredMode = NOT_REQUIRED)
	List<ActivityScrapingWeekResponse> activities
) {
}
