package sopio.acha.domain.fcm.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AlertRequest(
    @Schema(description = "알림 수신 상태")
    boolean status
) {
    public static AlertRequest of(Boolean alert) {
        return new AlertRequest(alert != null ? alert : true);
    }
}
