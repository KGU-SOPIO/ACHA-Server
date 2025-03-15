package sopio.acha.domain.fcm.application.exception;

import static sopio.acha.domain.fcm.application.exception.FcmExceptionCode.FCM_SEND_FAILED;

import sopio.acha.common.exception.CustomException;

public class FcmSendFailedException extends CustomException {
	public FcmSendFailedException() {
		super(FCM_SEND_FAILED);
	}
}
