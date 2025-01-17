package sopio.acha.domain.member.presentation.response;

import lombok.Builder;

@Builder
public record MemberTokenResponse(
	String accessToken,
	String refreshToken
) {
	public static MemberTokenResponse of(String accessToken, String refreshToken) {
		return MemberTokenResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}
}
