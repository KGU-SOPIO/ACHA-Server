package sopio.acha.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GlobalExceptionCode implements ExceptionCode {
	EXTRACTOR_ERROR(BAD_REQUEST, "추출기 오류가 발생했습니다."),
	INVALID_INPUT(BAD_REQUEST, "유효한 입력 형식이 아닙니다."),
	SERVER_ERROR(INTERNAL_SERVER_ERROR, "예상치 못한 문제가 발생했습니다."),
	KUTIS_PASSWORD_ERROR(UNAUTHORIZED, "KUTIS 비밀번호 변경이 필요합니다."),
	// KUTIS_SERVER_ERROR(INTERNAL_SERVER_ERROR, "KUTIS 서버에서 에러가 발생했습니다."),
	// LMS_SERVER_ERROR(INTERNAL_SERVER_ERROR, "LMS 서버에서 에러가 발생했습니다.")
	;

	private final HttpStatus status;
	private final String message;

	@Override
	public String getCode() {
		return this.name();
	}

}