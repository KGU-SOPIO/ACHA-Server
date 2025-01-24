package sopio.acha.domain.member.presentation.request;

public record MemberBasicInformationRequest(
	String name,
	String college,
	String department,
	String major
) {
}
