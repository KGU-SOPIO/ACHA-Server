package sopio.acha.domain.lecture.application;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestCourse;
import static sopio.acha.common.handler.ExtractorHandler.requestTimeTable;
import static sopio.acha.domain.lecture.domain.Lecture.save;

import java.util.List;
import java.util.stream.Collectors;

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
import sopio.acha.domain.memberLecture.application.MemberLectureService;

@Service
@RequiredArgsConstructor
public class LectureService {
	private final LectureRepository lectureRepository;
	private final MemberLectureService memberLectureService;

	public void extractLectureAndSave(Member currentMember) {
		JSONArray jsonArray = new JSONObject(
			requestCourse(currentMember.getId(), decrypt(currentMember.getPassword()))).getJSONArray("data");
		try {
			List<Lecture> extractedLectures = new ObjectMapper().readValue(
					jsonArray.toString(), new TypeReference<List<LectureBasicInformationResponse>>() {})
				.stream()
				.map(lecture -> save(lecture.title(), lecture.identifier(), lecture.professor()))
				.toList();
			List<Lecture> LecturesToSave = extractedLectures.stream()
				.filter(this::isExistsByIdentifier)
				.toList();
			if (!LecturesToSave.isEmpty()) lectureRepository.saveAll(LecturesToSave);
			memberLectureService.saveMyLectures(extractedLectures, currentMember);
		} catch (JsonProcessingException e) {
			throw new FailedParsingLectureDataException();
		}
	}

	private boolean isExistsByIdentifier(Lecture lecture) {
		return !lectureRepository.existsByIdentifier(lecture.getIdentifier());
	}

	/*
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
	*/
}
