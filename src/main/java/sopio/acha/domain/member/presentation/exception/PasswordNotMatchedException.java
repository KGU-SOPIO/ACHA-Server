package sopio.acha.domain.member.presentation.exception;

import static sopio.acha.domain.member.presentation.exception.MemberExceptionCode.PASSWORD_NOT_MATCHED;

import sopio.acha.common.exception.CustomException;

public class PasswordNotMatchedException extends CustomException {
	public PasswordNotMatchedException() {
		super(PASSWORD_NOT_MATCHED);
	}
}
