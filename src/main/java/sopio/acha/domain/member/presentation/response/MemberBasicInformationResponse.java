package sopio.acha.domain.member.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.member.domain.Member;

@Builder
public record MemberBasicInformationResponse (
	@Schema(description = "이름", requiredMode = REQUIRED)
	String name,

	@Schema(description = "학과", requiredMode = REQUIRED)
	String department,

	@Schema(description = "전공", requiredMode = NOT_REQUIRED)
	String major
){
	public static MemberBasicInformationResponse from(Member member) {
		return MemberBasicInformationResponse.builder()
			.name(member.getName())
			.department(member.getDepartment())
			.major(member.getMajor())
			.build();
	}
}
