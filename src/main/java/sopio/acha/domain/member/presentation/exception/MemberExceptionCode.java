package sopio.acha.domain.member.presentation.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sopio.acha.common.exception.ExceptionCode;

@Getter
@AllArgsConstructor
public enum MemberExceptionCode implements ExceptionCode {
	INVALID_STUDENT_ID_OR_PASSWORD(BAD_REQUEST, "학번 또는 비밀번호를 잘못 입력했습니다."),
	FAILED_PARSING_MEMBER_DATA(BAD_REQUEST, "회원 정보 파싱 중 에러가 발생했습니다."),
	INVALID_PASSWORD(BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
	MEMBER_NOT_AUTHENTICATED(FORBIDDEN, "해당 회원은 인증되지 않았습니다."),
	MEMBER_NOT_FOUND(NOT_FOUND, "아차 서비스의 회원이 아닙니다."),
	REFRESHTOKEN_NOT_FOUND(NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다.")
	;

	private final HttpStatus status;
	private final String message;

	@Override
	public String getCode() {
		return this.name();
	}
}
