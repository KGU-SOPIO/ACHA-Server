package sopio.acha.domain.activity.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.activity.application.ActivityService;
import sopio.acha.domain.activity.presentation.response.ActivitySummaryListResponse;
import sopio.acha.domain.activity.presentation.response.ActivityWeekListResponse;
import sopio.acha.domain.member.domain.Member;

@RestController
@Tag(name = "Activity", description = "활동 관리")
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {
	private final ActivityService activityService;

	@GetMapping("/my")
	@Operation(summary = "내 활동 목록 조회 API", description = "활동 제출 남은 시간을 기준으로 활동 목록을 조회합니다.")
	public ResponseEntity<ActivitySummaryListResponse> getMyActivityList(
		@CurrentMember Member currentMember
	) {
		ActivitySummaryListResponse response = activityService.getMyActivityList(currentMember);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/lecture")
	@Operation(summary = "강의별 활동 목록 조회 API", description = "강의별 활동 목록을 조회합니다.")
	public ResponseEntity<ActivityWeekListResponse> getLectureActivityList(
		@CurrentMember Member currentMember,
		@RequestParam String code
	) {
		ActivityWeekListResponse response = activityService.getLectureActivityList(currentMember, code);
		return ResponseEntity.ok().body(response);
	}
}
