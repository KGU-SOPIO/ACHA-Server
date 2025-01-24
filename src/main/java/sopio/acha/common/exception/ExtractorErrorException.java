package sopio.acha.common.exception;

import static sopio.acha.common.exception.GlobalExceptionCode.EXTRACTOR_ERROR;

public class ExtractorErrorException extends CustomException {
	public ExtractorErrorException() {
		super(EXTRACTOR_ERROR);
	}
}
