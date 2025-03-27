package sopio.acha.domain.fcm.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.fcm.application.FcmService;
import sopio.acha.domain.fcm.presentation.request.NotificationRequest;
import sopio.acha.domain.fcm.presentation.response.NotificationResponse;
import sopio.acha.domain.member.domain.Member;

@RestController
@Tag(name = "Fcm", description = "알림 기능")
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("")
    @Operation(summary = "알림 설정", description = "알림 기능을 켜고 끕니다.")
    public ResponseEntity<?> setNotificationStatus(
        @CurrentMember Member currentMember,
        @RequestBody NotificationRequest notificationRequest
        ) {
        fcmService.setNotificationStatus(currentMember, notificationRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    @Operation(summary = "알림 상태", description = "현재 유저의 알림 상태를 반환합니다.")
    public ResponseEntity<NotificationResponse> getNotificationStatus(
        @CurrentMember Member currentMember
    ) {
        NotificationResponse response = fcmService.getNotificationStatus(currentMember);
        return ResponseEntity.ok().body(response);
    }


}
