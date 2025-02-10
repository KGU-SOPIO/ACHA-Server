package sopio.acha.domain.member.presentation.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequest(
	@Schema(description = "리프레시토큰", requiredMode = REQUIRED)
	String refreshToken
) {
}
