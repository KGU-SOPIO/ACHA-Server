package sopio.acha.common.exception;

import static sopio.acha.common.exception.GlobalExceptionCode.CONVERT_ERROR;

public class ConvertErrorException extends CustomException {
	public ConvertErrorException() {
		super(CONVERT_ERROR);
	}
}
