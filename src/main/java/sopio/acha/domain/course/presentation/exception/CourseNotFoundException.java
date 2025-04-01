package sopio.acha.domain.course.presentation.exception;

import static sopio.acha.domain.course.presentation.exception.CourseExceptionCode.COURSE_NOT_FOUND;

import sopio.acha.common.exception.CustomException;

public class CourseNotFoundException extends CustomException {
	public CourseNotFoundException() {
		super(COURSE_NOT_FOUND);
	}
}
