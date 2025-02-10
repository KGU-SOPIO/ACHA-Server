package sopio.acha.domain.member.presentation.exception;

import static sopio.acha.domain.member.presentation.exception.MemberExceptionCode.FAILED_PARSING_MEMBER_DATA;

import sopio.acha.common.exception.CustomException;

public class FailedParsingMemberDataException extends CustomException {
	public FailedParsingMemberDataException() {
		super(FAILED_PARSING_MEMBER_DATA);
	}
}
