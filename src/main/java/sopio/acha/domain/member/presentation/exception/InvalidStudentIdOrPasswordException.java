package sopio.acha.domain.member.presentation.exception;

import static sopio.acha.domain.member.presentation.exception.MemberExceptionCode.INVALID_STUDENT_ID_OR_PASSWORD;

import sopio.acha.common.exception.CustomException;

public class InvalidStudentIdOrPasswordException extends CustomException {
	public InvalidStudentIdOrPasswordException() {
		super(INVALID_STUDENT_ID_OR_PASSWORD);
	}
}
