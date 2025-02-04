package sopio.acha.domain.lecture.presentation.response;

import static sopio.acha.common.handler.DateHandler.getToday;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.lecture.domain.Lecture;

@Builder
public record LectureSummaryListResponse(
	String date,
	List<LectureSummaryResponse> contents
) {
	public static LectureSummaryListResponse from(List<Lecture> lectures) {
		return LectureSummaryListResponse.builder()
			.date(getToday())
			.contents(lectures.stream()
				.map(LectureSummaryResponse::from)
				.toList())
			.build();
	}
}
