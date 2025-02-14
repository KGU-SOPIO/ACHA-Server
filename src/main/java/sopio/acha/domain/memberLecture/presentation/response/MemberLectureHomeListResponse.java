package sopio.acha.domain.memberLecture.presentation.response;

import java.util.List;

import lombok.Builder;
import sopio.acha.domain.memberLecture.domain.MemberLecture;

@Builder
public record MemberLectureHomeListResponse(
	List<MemberLectureHomeResponse> contents
) {
	public static MemberLectureHomeListResponse from(List<MemberLecture> contents) {
		return MemberLectureHomeListResponse.builder()
			.contents(contents.stream()
				.map(MemberLectureHomeResponse::from)
				.toList())
			.build();
	}
}
