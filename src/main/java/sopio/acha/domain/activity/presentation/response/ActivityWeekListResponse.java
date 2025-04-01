package sopio.acha.domain.activity.presentation.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import sopio.acha.domain.activity.domain.Activity;
import sopio.acha.domain.course.domain.Course;

@Builder
public record ActivityWeekListResponse(
	@Schema(description = "강의 제목", example = "컴퓨터 네트워크", requiredMode = REQUIRED)
	String courseName,

	@Schema(description = "교수 이름", example = "홍길동", requiredMode = REQUIRED)
	String professor,

	@Schema(description = "활동 목록", requiredMode = REQUIRED)
	List<ActivityWeekResponse> contents
) {
	public static ActivityWeekListResponse from(Course course, Map<Integer, List<Activity>> activities) {
		return ActivityWeekListResponse.builder()
			.courseName(course.getTitle())
			.professor(course.getProfessor())
			.contents(activities.entrySet().stream()
				.map(entry -> ActivityWeekResponse.from(entry.getKey(), entry.getValue()))
				.toList())
			.build();
	}
}
