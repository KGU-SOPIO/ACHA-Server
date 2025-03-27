package sopio.acha.domain.fcm.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AlertResponse(
    @Schema(description = "알림 상태")
    boolean status
) {
    public static AlertResponse of(Boolean alert) {
        return new AlertResponse(alert);
    }
}
