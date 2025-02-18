package sopio.acha.domain.member.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.member.domain.Member;

@Builder
public record MemberBasicInformationResponse (
	@Schema(description = "이름", requiredMode = REQUIRED)
	String name,

	@Schema(description = "대학", requiredMode = REQUIRED)
	String college,

	@Schema(description = "학과", requiredMode = REQUIRED)
	String affiliation
){
	public static MemberBasicInformationResponse from(Member member) {
		return MemberBasicInformationResponse.builder()
			.name(member.getName())
			.college(member.getCollege())
			.affiliation(member.getMajor() == null ? member.getDepartment() : member.getMajor())
			.build();
	}
}
