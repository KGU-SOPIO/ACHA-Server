package sopio.acha.domain.fcm.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record NotificationResponse (
    @Schema(description = "알림 상태")
    boolean status
) {
    public static NotificationResponse from(Boolean notification) {
        return NotificationResponse.builder().status(notification)
            .build();
    }
}
