package sopio.acha.domain.fcm.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeviceTokenRequest(
	@Schema(description = "디바이스 토큰", requiredMode = Schema.RequiredMode.REQUIRED)
	String deviceToken
) {
}
