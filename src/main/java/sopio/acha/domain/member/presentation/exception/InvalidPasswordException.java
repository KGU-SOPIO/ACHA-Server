package sopio.acha.domain.member.presentation.exception;

import static sopio.acha.domain.member.presentation.exception.MemberExceptionCode.INVALID_PASSWORD;

import sopio.acha.common.exception.CustomException;

public class InvalidPasswordException extends CustomException {
	public InvalidPasswordException() {
		super(INVALID_PASSWORD);
	}
}
