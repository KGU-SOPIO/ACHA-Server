package sopio.acha.domain.member.presentation.response;

public record MemberSummaryResponse(
	String name,
	String college,
	String department,
	String major
) {
}
