package sopio.acha.domain.lecture.application;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestTimeTable;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.common.handler.EncryptionHandler;
import sopio.acha.common.handler.ExtractorHandler;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.infrastructure.LectureRepository;
import sopio.acha.domain.lecture.presentation.response.LectureSummaryListResponse;
import sopio.acha.domain.member.application.MemberService;
import sopio.acha.domain.member.domain.Member;

@Service
@RequiredArgsConstructor
public class LectureService {
	private final MemberService memberService;
	private final LectureRepository lectureRepository;

	public void saveLecture(Member currentMember) {
		List<Object> lectureList = requestTimeTable(currentMember.getId(), decrypt(currentMember.getPassword()));
		List<Lecture> convertedLectureList = Lecture.convert(lectureList);
		lectureRepository.saveAll(convertedLectureList);
	}

	@Transactional(readOnly = true)
	public LectureSummaryListResponse getTodayLecture(Member currentMember) {
		List<Lecture> lectures = lectureRepository.findAllByMemberIdAndDayAndIsPresentTrue(currentMember.getId(),
			DateHandler.getTodayDate());
		return LectureSummaryListResponse.from(lectures);
	}
}
