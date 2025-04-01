package sopio.acha.domain.memberCourse.presentation.response;

import java.util.List;

import lombok.Builder;
import sopio.acha.domain.memberCourse.domain.MemberCourse;

@Builder
public record MemberCourseListResponse(
	List<MemberCourseResponse> contents
) {
	public static MemberCourseListResponse from(List<MemberCourse> contents) {
		return MemberCourseListResponse.builder()
			.contents(contents.stream()
				.map(MemberCourseResponse::from)
				.toList())
			.build();
	}
}
