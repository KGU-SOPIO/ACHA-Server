package sopio.acha.domain.lecture.presentation.response;

import java.util.List;

import lombok.Builder;
import sopio.acha.domain.lecture.domain.Lecture;

@Builder
public record LectureSummaryListResponse(
	List<LectureSummaryResponse> contents
) {
	public static LectureSummaryListResponse from(List<Lecture> lectures) {
		return LectureSummaryListResponse.builder()
			.contents(lectures.stream()
				.map(LectureSummaryResponse::from)
				.toList())
			.build();
	}
}