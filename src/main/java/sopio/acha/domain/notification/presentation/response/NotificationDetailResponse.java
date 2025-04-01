package sopio.acha.domain.notification.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.notification.domain.Notification;

@Builder
public record NotificationDetailResponse(
	@Schema(description = "공지 ID", example = "1", requiredMode = REQUIRED)
	Long id,

	@Schema(description = "강좌 이름", example = "운영체제", requiredMode = REQUIRED)
	String courseName,

	@Schema(description = "교수", example = "이병대", requiredMode = REQUIRED)
	String professor,

	@Schema(description = "공지 제목", example = "중간고사 일정 안내", requiredMode = REQUIRED)
	String title,

	@Schema(description = "공지 날짜", example = "2021-06-30", requiredMode = REQUIRED)
	String date,

	@Schema(description = "공지 내용", example = "중간고사 일정 안내입니다.", requiredMode = REQUIRED)
	String content,

	@Schema(description = "이전 공지", example = "{\"id\":\"1\",\"title\":\"중간고사 일정 안내\"}", requiredMode = REQUIRED)
	NotificationSummaryResponse prev,

	@Schema(description = "다음 공지", example = "{\"id\":\"1\",\"title\":\"중간고사 일정 안내\"}", requiredMode = REQUIRED)
	NotificationSummaryResponse next
) {
	public static NotificationDetailResponse of(Notification notification, Notification prev, Notification next) {
		return NotificationDetailResponse.builder()
			.id(notification.getId())
			.courseName(notification.getCourse().getTitle())
			.professor(notification.getCourse().getProfessor())
			.title(notification.getTitle())
			.date(notification.getDate())
			.content(notification.getContent())
			.prev(prev != null ? NotificationSummaryResponse.from(prev) : null)
			.next(next != null ? NotificationSummaryResponse.from(next) : null)
			.build();
	}
}
