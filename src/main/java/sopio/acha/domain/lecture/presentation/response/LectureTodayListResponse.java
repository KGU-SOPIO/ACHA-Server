package sopio.acha.domain.lecture.presentation.response;

import static sopio.acha.common.handler.DateHandler.getToday;

import java.util.List;

import lombok.Builder;
import sopio.acha.domain.lecture.domain.Lecture;

@Builder
public record LectureTodayListResponse(
	String date,
	List<LectureTodayResponse> contents
) {
	public static LectureTodayListResponse from(List<Lecture> lectures) {
		return LectureTodayListResponse.builder()
			.date(getToday())
			.contents(lectures.stream()
				.map(LectureTodayResponse::from)
				.toList())
			.build();
	}
}
