package sopio.acha.domain.member.presentation.exception;

import static sopio.acha.domain.member.presentation.exception.MemberExceptionCode.MEMBER_NOT_AUTHENTICATED;

import sopio.acha.common.exception.CustomException;

public class MemberNotAuthenticatedException extends CustomException {
	public MemberNotAuthenticatedException() {
		super(MEMBER_NOT_AUTHENTICATED);
	}
}
