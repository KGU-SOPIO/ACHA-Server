package sopio.acha.domain.activity.presentation.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sopio.acha.common.exception.ExceptionCode;

@Getter
@AllArgsConstructor
public enum ActivityExceptionCode implements ExceptionCode {
	FAILED_PARSING_ACTIVITY_DATA(BAD_REQUEST, "활동 정보 파싱 중 에러가 발생했습니다."),
	INVALID_ACTIVITY_TYPE(BAD_REQUEST, "올바르지 않은 활동 타입 입니다"),
	FAILED_SCHEDULE_ACTIVITY_EVENT(BAD_REQUEST, "활동 알림 등록 중 에러가 발생했습니다."),
	;

	private final HttpStatus status;
	private final String message;

	@Override
	public String getCode() {
		return this.name();
	}
}
