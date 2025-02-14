package sopio.acha.domain.lecture.presentation.exception;

import static sopio.acha.domain.lecture.presentation.exception.LectureExceptionCode.FAILED_PARSING_LECTURE_DATA;

import sopio.acha.common.exception.CustomException;

public class FailedParsingLectureDataException extends CustomException {
	public FailedParsingLectureDataException() {
		super(FAILED_PARSING_LECTURE_DATA);
	}
}
