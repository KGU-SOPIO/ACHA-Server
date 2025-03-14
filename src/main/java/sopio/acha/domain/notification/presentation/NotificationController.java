package sopio.acha.domain.notification.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.domain.notification.application.NotificationService;
import sopio.acha.domain.notification.presentation.response.NotificationListResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "공지 API")
public class NotificationController {
	private final NotificationService notificationService;

	@GetMapping
	@Operation(summary = "강좌별 공지사항 목록 조회 API",description = "강좌별 공지사항 목록을 불러옵니다")
	public ResponseEntity<NotificationListResponse> notifications(
		@RequestParam String code
	) {
		NotificationListResponse response = notificationService.getNotifications(code);
		return ResponseEntity.ok(response);
	}
}
