package sopio.acha.domain.member.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 7)
public class RefreshToken {
	@Id
	private String studentId;

	@Indexed
	private String refreshToken;

	public static RefreshToken of(String studentId, String refreshToken) {
		return RefreshToken.builder()
			.studentId(studentId)
			.refreshToken(refreshToken)
			.build();
	}
}