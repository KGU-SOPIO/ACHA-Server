package sopio.acha.domain.fcm.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AlertRequest(
    @Schema(description = "알림 상태")
    boolean status
) {
    public static AlertRequest of(Boolean alert) {
        if (alert == null)
            alert = true;
        return new AlertRequest(alert);
    }
}
