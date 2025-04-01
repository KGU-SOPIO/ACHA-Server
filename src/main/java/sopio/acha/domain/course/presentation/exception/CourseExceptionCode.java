package sopio.acha.domain.course.presentation.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sopio.acha.common.exception.ExceptionCode;

@Getter
@AllArgsConstructor
public enum CourseExceptionCode implements ExceptionCode {
	FAILED_PARSING_COURSE_DATA(BAD_REQUEST, "강좌 정보 파싱 중 에러가 발생했습니다."),
	COURSE_NOT_FOUND(NOT_FOUND, "강좌를 찾을 수 없습니다."),
	;

	private final HttpStatus status;
	private final String message;

	@Override
	public String getCode() {
		return this.name();
	}
}
