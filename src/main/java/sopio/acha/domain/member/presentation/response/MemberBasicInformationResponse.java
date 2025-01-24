package sopio.acha.domain.member.presentation.response;

import lombok.Builder;
import sopio.acha.domain.member.domain.Member;

@Builder
public record MemberBasicInformationResponse (
	String name,
	String department,
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
