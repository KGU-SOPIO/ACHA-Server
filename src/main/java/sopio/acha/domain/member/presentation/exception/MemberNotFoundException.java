package sopio.acha.domain.member.presentation.exception;

import static sopio.acha.domain.member.presentation.exception.MemberExceptionCode.MEMBER_NOT_FOUND;

import sopio.acha.common.exception.CustomException;

public class MemberNotFoundException extends CustomException {
	public MemberNotFoundException() {
		super(MEMBER_NOT_FOUND);
	}
}
