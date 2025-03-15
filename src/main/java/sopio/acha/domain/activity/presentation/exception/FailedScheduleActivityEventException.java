package sopio.acha.domain.activity.presentation.exception;

import static sopio.acha.domain.activity.presentation.exception.ActivityExceptionCode.FAILED_SCHEDULE_ACTIVITY_EVENT;

import sopio.acha.common.exception.CustomException;

public class FailedScheduleActivityEventException extends CustomException {
	public FailedScheduleActivityEventException() {
		super(FAILED_SCHEDULE_ACTIVITY_EVENT);
	}
}
