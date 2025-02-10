package sopio.acha.domain.member.presentation.exception;

import static sopio.acha.domain.member.presentation.exception.MemberExceptionCode.REFRESHTOKEN_NOT_FOUND;

import sopio.acha.common.exception.CustomException;

public class RefreshTokenNotFoundException extends CustomException {
	public RefreshTokenNotFoundException() {
		super(REFRESHTOKEN_NOT_FOUND);
	}
}
