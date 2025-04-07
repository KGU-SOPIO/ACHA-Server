package sopio.acha.domain.member.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record MemberTokenResponse(
	@Schema(description = "액세스 토큰", requiredMode = REQUIRED)
	String accessToken,

	@Schema(description = "리프레시 토큰", requiredMode = REQUIRED)
	String refreshToken,

	@Schema(description = "추출 완료 상태", requiredMode = REQUIRED)
	Boolean extract
) {
	public static MemberTokenResponse of(String accessToken, String refreshToken, Boolean extract) {
		return MemberTokenResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.extract(extract)
			.build();
	}
}
