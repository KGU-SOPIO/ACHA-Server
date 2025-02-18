package sopio.acha.domain.activity.presentation.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sopio.acha.common.exception.ExceptionCode;

@Getter
@AllArgsConstructor
public enum ActivityExceptionCode implements ExceptionCode {
	INVALID_ACTIVITY_TYPE(BAD_REQUEST, "올바르지 않은 활동 타입 입니다")
	;

	private final HttpStatus status;
	private final String message;

	@Override
	public String getCode() {
		return this.name();
	}
}
