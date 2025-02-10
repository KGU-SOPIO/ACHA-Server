package sopio.acha.common.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String studentId, String refreshToken) {
        long durationInSeconds = Duration.ofDays(7).getSeconds();
        redisTemplate.opsForValue().set(
                "refresh:" + studentId,
                refreshToken,
                durationInSeconds,
                TimeUnit.SECONDS
        );
    }

    public boolean validateRefreshToken(String studentId, String refreshToken) {
        String storedToken = getRefreshToken(studentId);
        if (storedToken == null) {
            throw new IllegalArgumentException("refresh token이 존재하지 않거나 만료되었습니다.");
        }
        return storedToken.equals(refreshToken);
    }

    public String getRefreshToken(String studentId) {
        return redisTemplate.opsForValue().get("refresh:" + studentId);
    }

}
