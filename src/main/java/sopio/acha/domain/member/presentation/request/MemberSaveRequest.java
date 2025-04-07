package sopio.acha.domain.member.presentation.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties
public record MemberSaveRequest(
	@Schema(description = "학번", requiredMode = REQUIRED)
	String studentId,

	@Schema(description = "비밀번호", requiredMode = REQUIRED)
	String password,

	@Schema(description = "이름", requiredMode = REQUIRED)
	String name,

	@Schema(description = "대학", requiredMode = REQUIRED)
	String college,

	@Schema(description = "학부", requiredMode = REQUIRED)
	String department,

	@Schema(description = "전공", requiredMode = NOT_REQUIRED)
	String major,

	@Schema(description = "기기 고유 토큰", requiredMode = NOT_REQUIRED)
	String deviceToken
) {
}
