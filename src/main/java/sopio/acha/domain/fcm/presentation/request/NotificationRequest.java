package sopio.acha.domain.fcm.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.fcm.presentation.response.NotificationResponse;

@Builder
public record NotificationRequest (
    @Schema(description = "알림 상태")
    boolean status
) {
    public static NotificationRequest from(Boolean notification) {
        return NotificationRequest.builder().status(notification)
            .build();
    }
}
