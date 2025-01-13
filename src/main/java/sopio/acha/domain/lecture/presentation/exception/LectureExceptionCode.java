package sopio.acha.domain.lecture.presentation.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sopio.acha.common.exception.ExceptionCode;

@Getter
@AllArgsConstructor
public enum LectureExceptionCode implements ExceptionCode {
	;

	private final HttpStatus status;
	private final String message;

	@Override
	public String getCode() {
		return this.name();
	}
}
