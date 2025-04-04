package sopio.acha.domain.course.application;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.infrastructure.CourseRepository;
import sopio.acha.domain.course.presentation.exception.CourseNotFoundException;
import sopio.acha.domain.course.presentation.response.CourseBasicInformationResponse;
import sopio.acha.domain.notification.application.NotificationService;

@Component
@RequiredArgsConstructor
public class NoticeExtractor {
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;

    public void extractAndSave(Map<String, CourseBasicInformationResponse> courseMap) {
        courseMap.values().stream()
			.filter(course -> course.notices() != null && !course.notices().isEmpty())
			.forEach(course -> {
				Course existingCourse = courseRepository.findByIdentifier(course.identifier())
					.orElseThrow(CourseNotFoundException::new);
				notificationService.extractNotifications(course.notices(), existingCourse);
			});
    }
}