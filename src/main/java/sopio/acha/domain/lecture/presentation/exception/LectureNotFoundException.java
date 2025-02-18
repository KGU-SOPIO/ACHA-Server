package sopio.acha.domain.lecture.presentation.exception;

import static sopio.acha.domain.lecture.presentation.exception.LectureExceptionCode.LECTURE_NOT_FOUND;

import sopio.acha.common.exception.CustomException;

public class LectureNotFoundException extends CustomException {
	public LectureNotFoundException() {
		super(LECTURE_NOT_FOUND);
	}
}
