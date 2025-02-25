package sopio.acha.domain.activity.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.activity.application.ActivityService;
import sopio.acha.domain.member.domain.Member;

@RestController
@Tag(name = "Activity", description = "활동 관리")
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {
	private final ActivityService activityService;

	@PostMapping
	@Operation(summary = "활동 정보 스크래핑 요청 API", description = "활동 정보를 스크래핑하고 해당 데이터를 DB에 저장합니다.")
	public ResponseEntity<Void> extractActivityAndSave(
		@CurrentMember Member currentMember
	) {
		activityService.extractActivity(currentMember);
		return ResponseEntity.ok().build();
	}
}
