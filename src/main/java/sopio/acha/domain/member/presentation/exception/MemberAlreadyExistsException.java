package sopio.acha.domain.member.presentation.exception;

import static sopio.acha.domain.member.presentation.exception.MemberExceptionCode.MEMBER_ALREADY_EXISTS;

import sopio.acha.common.exception.CustomException;

public class MemberAlreadyExistsException extends CustomException {
	public MemberAlreadyExistsException() {
		super(MEMBER_ALREADY_EXISTS);
	}
}
