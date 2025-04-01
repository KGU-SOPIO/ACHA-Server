package sopio.acha.domain.course.presentation.exception;

import static sopio.acha.domain.course.presentation.exception.CourseExceptionCode.FAILED_PARSING_COURSE_DATA;

import sopio.acha.common.exception.CustomException;

public class FailedParsingCourseDataException extends CustomException {
	public FailedParsingCourseDataException() {
		super(FAILED_PARSING_COURSE_DATA);
	}
}
