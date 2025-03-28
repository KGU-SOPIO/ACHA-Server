package sopio.acha.domain.lecture.application;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.infrastructure.LectureRepository;
import sopio.acha.domain.lecture.presentation.exception.LectureNotFoundException;
import sopio.acha.domain.lecture.presentation.response.LectureBasicInformationResponse;
import sopio.acha.domain.notification.application.NotificationService;

@Component
@RequiredArgsConstructor
public class NoticeExtractor {
    private final LectureRepository lectureRepository;
    private final NotificationService notificationService;

    public void extractAndSave(Map<String, LectureBasicInformationResponse> courseMap) {
        courseMap.values().stream()
			.filter(course -> course.notices() != null && !course.notices().isEmpty())
			.forEach(course -> {
				Lecture lecture = lectureRepository.findByIdentifier(course.identifier())
					.orElseThrow(LectureNotFoundException::new);
				notificationService.extractNotifications(course.notices(), lecture);
			});
    }
}