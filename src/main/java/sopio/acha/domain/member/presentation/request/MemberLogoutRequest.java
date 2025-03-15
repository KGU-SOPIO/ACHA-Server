package sopio.acha.domain.member.presentation.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberLogoutRequest(
	@Schema(description = "디바이스 토큰", requiredMode = REQUIRED)
	String deviceToken
) {
}
