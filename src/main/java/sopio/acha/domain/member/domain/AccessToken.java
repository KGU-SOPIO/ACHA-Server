package sopio.acha.domain.member.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccessToken {
	private String accessToken;

	public static AccessToken of(String accessToken) {
		return AccessToken.builder()
			.accessToken(accessToken)
			.build();
	}
}
