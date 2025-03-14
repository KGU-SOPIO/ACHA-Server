package sopio.acha.domain.notification.presentation.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sopio.acha.common.exception.ExceptionCode;

@Getter
@AllArgsConstructor
public enum NotificationExceptionCode implements ExceptionCode {
	NOTIFICATION_NOT_FOUND(NOT_FOUND, "공지사항이 없습니다"),
	;

	private final HttpStatus status;
	private final String message;

	@Override
	public String getCode() {
		return this.name();
	}
}
