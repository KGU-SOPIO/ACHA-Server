package sopio.acha.domain.activity.application;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;

@Service
@RequiredArgsConstructor
public class ActivityService {
	private final ActivityRepository activityRepository;

}
