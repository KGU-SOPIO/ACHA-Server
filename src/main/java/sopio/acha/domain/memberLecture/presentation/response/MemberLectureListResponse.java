package sopio.acha.domain.memberLecture.presentation.response;

import java.util.List;

import lombok.Builder;
import sopio.acha.domain.memberLecture.domain.MemberLecture;

@Builder
public record MemberLectureListResponse(
	List<MemberLectureResponse> contents
) {
	public static MemberLectureListResponse from(List<MemberLecture> contents) {
		return MemberLectureListResponse.builder()
			.contents(contents.stream()
				.map(MemberLectureResponse::from)
				.toList())
			.build();
	}
}
