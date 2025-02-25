package sopio.acha.domain.activity.presentation.exception;

import static sopio.acha.domain.activity.presentation.exception.ActivityExceptionCode.FAILED_PARSING_ACTIVITY_DATA;

import sopio.acha.common.exception.CustomException;

public class FailedParsingActivityDataException extends CustomException {
	public FailedParsingActivityDataException() {
		super(FAILED_PARSING_ACTIVITY_DATA);
	}
}
