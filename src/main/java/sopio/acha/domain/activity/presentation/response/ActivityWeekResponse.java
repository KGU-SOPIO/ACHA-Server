package sopio.acha.domain.activity.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.activity.domain.Activity;

@Builder
public record ActivityWeekResponse(
	@Schema(description = "주차", example = "2", requiredMode = REQUIRED)
	int week,

	List<ActivityWeekDetailResponse> contents
) {
	public static ActivityWeekResponse from(int week, List<Activity> activities) {
		return ActivityWeekResponse.builder()
			.week(week)
			.contents(activities.stream()
				.map(ActivityWeekDetailResponse::from)
				.toList())
			.build();
	}
}
