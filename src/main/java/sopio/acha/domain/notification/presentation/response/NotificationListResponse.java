package sopio.acha.domain.notification.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.notification.domain.Notification;

@Builder
public record NotificationListResponse(
	@Schema(description = "강좌 이름", example = "운영체제", requiredMode = REQUIRED)
	String courseName,

	@Schema(description = "공지 목록",
		example = "[{\"id\":\"1\",\"title\":\"중간고사 일정 안내\",\"professor\":\"이병대\",\"date\":\"2021-06-30\",\"index\":1}]",
		requiredMode = REQUIRED)
	List<NotificationResponse> contents
) {
	public static NotificationListResponse from(String courseName, List<Notification> notifications) {
		return NotificationListResponse.builder()
			.courseName(courseName)
			.contents(notifications.stream().map(NotificationResponse::from).collect(Collectors.toList()))
			.build();
	}
}
