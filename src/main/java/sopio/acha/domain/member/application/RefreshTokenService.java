package sopio.acha.domain.member.application;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.member.domain.RefreshToken;
import sopio.acha.domain.member.infrastructure.RefreshTokenRepository;
import sopio.acha.domain.member.presentation.exception.RefreshTokenNotFoundException;
import sopio.acha.domain.member.presentation.request.RefreshTokenRequest;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;

	public void saveRefreshToken(RefreshToken generatedRefreshToken) {
		refreshTokenRepository.save(generatedRefreshToken);
	}

	public RefreshToken getRefreshTokenObject(RefreshTokenRequest request) {
		return refreshTokenRepository.findByRefreshToken(request.refreshToken())
			.orElseThrow(RefreshTokenNotFoundException::new);
	}
}
