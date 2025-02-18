package sopio.acha.domain.memberActivity.presentation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.domain.memberActivity.application.MemberActivityService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member-activities")
@Tag(name = "Member Activity", description = "사용자 활동 API")
public class MemberActivityController {
	private final MemberActivityService memberActivityService;
}
