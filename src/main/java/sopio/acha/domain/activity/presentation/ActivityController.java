package sopio.acha.domain.activity.presentation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.domain.activity.application.ActivityService;

@RestController
@Tag(name = "Activity", description = "활동 관리")
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {
	private final ActivityService activityService;
}
