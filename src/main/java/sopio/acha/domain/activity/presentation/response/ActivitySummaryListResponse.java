package sopio.acha.domain.activity.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.activity.domain.Activity;

@Builder
public record ActivitySummaryListResponse(
	@Schema(description = "활동 목록", requiredMode = REQUIRED)
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
