package sopio.acha.domain.lecture.application;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestTimeTable;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.domain.LectureDay;
import sopio.acha.domain.lecture.infrastructure.LectureRepository;
import sopio.acha.domain.lecture.presentation.response.LectureSummaryListResponse;
import sopio.acha.domain.lecture.presentation.response.LectureTodayListResponse;
import sopio.acha.domain.member.domain.Member;

@Service
@RequiredArgsConstructor
public class LectureService {
	private final LectureRepository lectureRepository;

	@Transactional
	public LectureTodayListResponse getTodayLecture(Member currentMember) {
		validateHasLecture(currentMember);
		List<Lecture> lectures = lectureRepository.findAllByMemberIdAndDayAndIsPresentTrueOrderByStartAtAsc(currentMember.getId(),
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
		if (lectureRepository.existsByMemberId(currentMember.getId())) return;
		List<Object> lectureList = requestTimeTable(currentMember.getId(), decrypt(currentMember.getPassword()));
		lectureRepository.saveAll(Lecture.convert(lectureList, currentMember));
	}
}
