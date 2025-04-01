package sopio.acha.domain.fcm.application.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sopio.acha.common.exception.ExceptionCode;

@Getter
@AllArgsConstructor
public enum FcmExceptionCode implements ExceptionCode {
	FCM_SEND_FAILED(INTERNAL_SERVER_ERROR, "FCM 메시지 전송에 실패했습니다"),
	;

	private final HttpStatus status;
	private final String message;

	@Override
	public String getCode() {
		return this.name();
	}
}
