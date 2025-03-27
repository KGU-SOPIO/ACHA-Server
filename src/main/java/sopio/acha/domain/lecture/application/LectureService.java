package sopio.acha.domain.lecture.application;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestCourse;
import static sopio.acha.common.handler.ExtractorHandler.requestTimeTable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.infrastructure.LectureRepository;
import sopio.acha.domain.lecture.presentation.exception.FailedParsingLectureDataException;
import sopio.acha.domain.lecture.presentation.exception.LectureNotFoundException;
import sopio.acha.domain.lecture.presentation.response.LectureBasicInformationResponse;
import sopio.acha.domain.lecture.presentation.response.LectureTimeTableResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.application.MemberLectureService;
import sopio.acha.domain.notification.application.NotificationService;

@Service
@RequiredArgsConstructor
public class LectureService {
	private final LectureRepository lectureRepository;
	private final MemberLectureService memberLectureService;
	private final NotificationService notificationService;
	private final ExtractActivities extractActivities;
	private final ObjectMapper objectMapper;

	@Transactional(propagation = REQUIRES_NEW)
	public void extractLectureAndSave(Member currentMember) {
		try {
			String decryptedPassword = decrypt(currentMember.getPassword());

			JsonNode timeTableData = objectMapper.readTree(
					requestTimeTable(currentMember.getId(), decryptedPassword)).get("data");
			JsonNode courseData = objectMapper.readTree(
					requestCourse(currentMember.getId(), decryptedPassword)).get("data");

			List<LectureBasicInformationResponse> courseList = StreamSupport.stream(courseData.spliterator(), false)
					.map(node -> objectMapper.convertValue(node, LectureBasicInformationResponse.class))
					.toList();
			Map<String, LectureBasicInformationResponse> courseMap = courseList.stream()
					.collect(Collectors.toMap(LectureBasicInformationResponse::identifier, Function.identity()));

			List<Lecture> newCourses = courseMap.values().stream()
					.map(course -> Lecture.save(
							course.title(),
							course.identifier(),
							course.code(),
							course.professor()))
					.filter(this::isExistsByIdentifier)
					.toList();
			if (!newCourses.isEmpty()) {
				lectureRepository.saveAll(newCourses);
			}

			courseMap.values().stream()
					.filter(course -> course.notices() != null && !course.notices().isEmpty())
					.forEach(course -> {
						Lecture lecture = lectureRepository.findByIdentifier(course.identifier())
								.orElseThrow(LectureNotFoundException::new);
						notificationService.extractNotifications(course.notices(), lecture);
					});

			List<LectureTimeTableResponse> timeTableList = StreamSupport.stream(timeTableData.spliterator(), false)
					.map(node -> objectMapper.convertValue(node, LectureTimeTableResponse.class))
					.toList();

			Map<String, LectureTimeTableResponse> timeTableMap = timeTableList.stream()
					.collect(Collectors.toMap(LectureTimeTableResponse::identifier, Function.identity()));

			timeTableMap.forEach((identifier, timeTable) -> {
				Lecture lecture = lectureRepository.findByIdentifier(identifier)
						.orElseThrow(LectureNotFoundException::new);
				lecture.setTimeTable(
						timeTable.day(),
						timeTable.classTime(),
						timeTable.startAt(),
						timeTable.endAt(),
						timeTable.lectureRoom()
				);
			});

			List<Lecture> coursesWithTimetable = timeTableMap.keySet().stream()
					.map(this::getByIdentifier)
					.toList();

			memberLectureService.saveMyLectures(coursesWithTimetable, currentMember);
			extractActivities.saveLectureAndActivities(courseData, coursesWithTimetable, currentMember, objectMapper);
		} catch (JsonProcessingException e) {
			throw new FailedParsingLectureDataException();
		}
	}

	public Lecture getLectureByCode(String code) {
		return lectureRepository.findByCode(code)
			.orElseThrow(LectureNotFoundException::new);
	}

	private Lecture getByIdentifier(String identifier) {
		return lectureRepository.findByIdentifier(identifier)
			.orElseThrow(LectureNotFoundException::new);
	}

	private boolean isExistsByIdentifier(Lecture lecture) {
		return !lectureRepository.existsByIdentifier(lecture.getIdentifier());
	}
}
