package sopio.acha.domain.member.presentation.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties
public record MemberLoginRequest(
	@Schema(description = "학번", requiredMode = REQUIRED)
	String studentId,

	@Schema(description = "비밀번호", requiredMode = REQUIRED)
	String password,

	@Schema(description = "디바이스 토큰", requiredMode = NOT_REQUIRED)
	String deviceToken
) {
}
