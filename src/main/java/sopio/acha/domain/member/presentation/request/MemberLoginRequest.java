package sopio.acha.domain.member.presentation.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberLoginRequest(
	@Schema(description = "학번", requiredMode = REQUIRED)
	String studentId,

	@Schema(description = "비밀번호", requiredMode = REQUIRED)
	String password
) {
}
