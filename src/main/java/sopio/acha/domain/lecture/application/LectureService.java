package sopio.acha.domain.lecture.application;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestCourse;
import static sopio.acha.common.handler.ExtractorHandler.requestTimeTable;
import static sopio.acha.domain.lecture.domain.Lecture.save;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.domain.LectureDay;
import sopio.acha.domain.lecture.infrastructure.LectureRepository;
import sopio.acha.domain.lecture.presentation.exception.FailedParsingLectureDataException;
import sopio.acha.domain.lecture.presentation.response.LectureBasicInformationResponse;
import sopio.acha.domain.lecture.presentation.response.LectureSummaryListResponse;
import sopio.acha.domain.lecture.presentation.response.LectureTodayListResponse;
import sopio.acha.domain.member.domain.Member;

@Service
@RequiredArgsConstructor
public class LectureService {
	private final LectureRepository lectureRepository;

	public void extractLectureAndSave(Member currentMember) {
		JSONArray jsonArray = new JSONObject(
			requestCourse(currentMember.getId(), decrypt(currentMember.getPassword()))).getJSONArray("data");
		try {
			List<LectureBasicInformationResponse> lectureList = new ObjectMapper().readValue(
				jsonArray.toString(), new TypeReference<List<LectureBasicInformationResponse>>() {
				}
			);
			List<Lecture> newLectures = lectureList.stream()
				.filter(this::isExistsByIdentifier)
				.map(lecture -> save(lecture.title(), lecture.identifier(), lecture.professor()))
				.toList();
			if (!newLectures.isEmpty()) lectureRepository.saveAll(newLectures);
		} catch (JsonProcessingException e) {
			throw new FailedParsingLectureDataException();
		}
	}

	private boolean isExistsByIdentifier(LectureBasicInformationResponse lecture) {
		return !lectureRepository.existsByIdentifier(lecture.identifier());
	}

	@Transactional
	public LectureTodayListResponse getTodayLecture(Member currentMember) {
		validateHasLecture(currentMember);
		List<Lecture> lectures = lectureRepository.findAllByMemberIdAndDayAndIsPresentTrueOrderByStartAtAsc(
			currentMember.getId(),
			LectureDay.valueOf(DateHandler.getTodayDate()));
		return LectureTodayListResponse.from(lectures);
	}

	@Transactional
	public LectureSummaryListResponse getAllMyLectureList(Member currentMember) {
		validateHasLecture(currentMember);
		List<Lecture> lectures = lectureRepository.findAllByMemberIdOrderByDayOrderAscStartAtAsc(currentMember.getId());
		return LectureSummaryListResponse.from(lectures);
	}

	private void validateHasLecture(Member currentMember) {
		if (lectureRepository.existsByMemberId(currentMember.getId()))
			return;
		List<Object> lectureList = requestTimeTable(currentMember.getId(), decrypt(currentMember.getPassword()));
		lectureRepository.saveAll(Lecture.convert(lectureList, currentMember));
	}

}
