package sopio.acha.domain.notification.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.notification.domain.Notification;

@Builder
public record NotificationSummaryResponse(
	@Schema(description = "공지 ID", example = "1", requiredMode = REQUIRED)
	Long id,

	@Schema(description = "공지 제목", example = "중간고사 일정 안내", requiredMode = REQUIRED)
	String title
) {
	public static NotificationSummaryResponse from(Notification notification) {
		return NotificationSummaryResponse.builder()
			.id(notification.getId())
			.title(notification.getTitle())
			.build();
	}
}
