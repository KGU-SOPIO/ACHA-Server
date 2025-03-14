package sopio.acha.domain.notification.presentation.exception;

import static sopio.acha.domain.notification.presentation.exception.NotificationExceptionCode.NOTIFICATION_NOT_FOUND;

import sopio.acha.common.exception.CustomException;

public class NotificationNotFoundException extends CustomException {
	public NotificationNotFoundException() {
		super(NOTIFICATION_NOT_FOUND);
	}
}
