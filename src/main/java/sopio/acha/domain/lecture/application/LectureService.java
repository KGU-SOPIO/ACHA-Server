package sopio.acha.domain.lecture.application;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestCourse;
import static sopio.acha.common.handler.ExtractorHandler.requestTimeTable;
import static sopio.acha.domain.lecture.domain.Lecture.save;

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

	@Transactional(propagation = REQUIRES_NEW)
	public void extractLectureAndSave(Member currentMember) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode courseData = objectMapper.readTree(
				requestCourse(currentMember.getId(), decrypt(currentMember.getPassword()))).get("data");

			List<Lecture> lectures = StreamSupport.stream(courseData.spliterator(), false)
				.map(node -> objectMapper.convertValue(node, LectureBasicInformationResponse.class))
				.map(lectureData -> save(lectureData.title(), lectureData.identifier(), lectureData.code(),
					lectureData.professor()))
				.filter(this::isExistsByIdentifier)
				.toList();
			if (!lectures.isEmpty()) lectureRepository.saveAll(lectures);

			StreamSupport.stream(courseData.spliterator(), false)
				.map(node -> objectMapper.convertValue(node, LectureBasicInformationResponse.class))
				.filter(lectureData -> lectureData.notices() != null && !lectureData.notices().isEmpty())
				.forEach(lectureData -> {
					Lecture lecture = getLectureByCode(lectureData.code());
					notificationService.extractNotifications(lectureData.notices(), lecture);
				});

			JsonNode timeTableData = objectMapper.readTree(
				requestTimeTable(currentMember.getId(), decrypt(currentMember.getPassword()))).get("data");
			Map<String, LectureTimeTableResponse> timeTableMap = StreamSupport.stream(timeTableData.spliterator(),
					false)
				.map(node -> objectMapper.convertValue(node, LectureTimeTableResponse.class))
				.collect(Collectors.toMap(LectureTimeTableResponse::identifier, Function.identity()));
			lectures.forEach(lecture -> {
				LectureTimeTableResponse timeTable = timeTableMap.get(lecture.getIdentifier());
				if (timeTable != null) {
					lecture.setTimeTable(
						timeTable.day(), timeTable.classTime(), timeTable.startAt(), timeTable.endAt(),
						timeTable.lectureRoom()
					);
				}
			});
			List<Lecture> lectureHasTimeTable = StreamSupport.stream(timeTableData.spliterator(), false)
				.map(node -> objectMapper.convertValue(node, LectureTimeTableResponse.class))
				.map(LectureTimeTableResponse::identifier)
				.map(this::getByIdentifier)
				.toList();
			memberLectureService.saveMyLectures(lectureHasTimeTable, currentMember);
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
