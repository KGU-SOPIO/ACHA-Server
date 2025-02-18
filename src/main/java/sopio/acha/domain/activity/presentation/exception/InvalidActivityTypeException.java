package sopio.acha.domain.activity.presentation.exception;

import static sopio.acha.domain.activity.presentation.exception.ActivityExceptionCode.INVALID_ACTIVITY_TYPE;

import sopio.acha.common.exception.CustomException;

public class InvalidActivityTypeException extends CustomException {
	public InvalidActivityTypeException() {
		super(INVALID_ACTIVITY_TYPE);
	}
}
