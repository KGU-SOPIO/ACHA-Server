package sopio.acha.domain.activity.presentation.response;

import java.util.List;

import lombok.Builder;
import sopio.acha.domain.activity.domain.Activity;

@Builder
public record ActivitySummaryListResponse(
	List<ActivitySummaryResponse> contents
) {
	public static ActivitySummaryListResponse from(List<Activity> activities) {
		return ActivitySummaryListResponse.builder()
			.contents(activities.stream()
				.map(ActivitySummaryResponse::from)
				.toList())
			.build();
	}
}
