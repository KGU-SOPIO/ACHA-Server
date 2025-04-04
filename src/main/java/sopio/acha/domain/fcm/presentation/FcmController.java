package sopio.acha.domain.fcm.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.fcm.application.FcmService;
import sopio.acha.domain.fcm.presentation.request.AlertRequest;
import sopio.acha.domain.fcm.presentation.request.DeviceTokenRequest;
import sopio.acha.domain.fcm.presentation.response.AlertResponse;
import sopio.acha.domain.member.domain.Member;

@RestController
@Tag(name = "Fcm", description = "알림 기능")
@RequestMapping("/api/v1/alert")
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;

    @PostMapping
    @Operation(summary = "알림 설정", description = "알림 기능을 켜고 끕니다.")
    public ResponseEntity<?> setAlertStatus(
        @CurrentMember Member currentMember,
        @RequestBody AlertRequest alertRequest
        ) {
        fcmService.setAlertStatus(currentMember, alertRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "알림 상태", description = "현재 유저의 알림 상태를 반환합니다.")
    public ResponseEntity<AlertResponse> getAlertStatus(
        @CurrentMember Member currentMember
    ) {
        AlertResponse response = fcmService.getAlertStatus(currentMember);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/fcm-token")
    @Operation(summary = "디바이스 토큰 추가", description = "디바이스 토큰을 추가로 저장합니다")
    public ResponseEntity<Void> addFcmToken(
        @CurrentMember Member currentMember,
        @RequestBody DeviceTokenRequest request
    ) {
        fcmService.addFcmToken(currentMember, request.deviceToken());
        return ResponseEntity.ok().build();
    }
}
