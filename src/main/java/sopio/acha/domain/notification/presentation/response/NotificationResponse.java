package sopio.acha.domain.notification.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.notification.domain.Notification;

@Builder
public record NotificationResponse(
	@Schema(description = "공지사항 ID", example = "1", requiredMode = REQUIRED)
	Long id,

	@Schema(description = "공지 제목", example = "중간고사 일정 안내", requiredMode = REQUIRED)
	String title,

	@Schema(description = "교수", example = "이병대", requiredMode = REQUIRED)
	String professor,

	@Schema(description = "공지 날짜", example = "2021-06-30", requiredMode = REQUIRED)
	String date,

	@Schema(description = "공지 번호", example = "1", requiredMode = REQUIRED)
	String index
) {
	public static NotificationResponse from(Notification notification) {
		return NotificationResponse.builder()
			.id(notification.getId())
			.title(notification.getTitle())
			.professor(notification.getCourse().getProfessor())
			.date(notification.getDate())
			.index(notification.getIndex() == 9999 ? "중요" : String.valueOf(notification.getIndex()))
			.build();
	}
}
