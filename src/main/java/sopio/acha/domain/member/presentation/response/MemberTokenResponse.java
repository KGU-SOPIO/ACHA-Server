package sopio.acha.domain.member.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record MemberTokenResponse(
	@Schema(description = "액세스토큰", requiredMode = REQUIRED)
	String accessToken,

	@Schema(description = "리프레시토큰", requiredMode = REQUIRED)
	String refreshToken
) {
	public static MemberTokenResponse of(String accessToken, String refreshToken) {
		return MemberTokenResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}
}
