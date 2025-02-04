package sopio.acha.domain.lecture.application;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestTimeTable;

import java.util.List;

import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
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
	private final SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer;

	@Transactional
	public LectureSummaryListResponse getTodayLecture(Member currentMember) {
		validateHasLecture(currentMember);
		List<Lecture> lectures = lectureRepository.findAllByMemberIdAndDayAndIsPresentTrue(currentMember.getId(),
			DateHandler.getTodayDate());
		return LectureSummaryListResponse.from(lectures);
	}

	private void validateHasLecture(Member currentMember) {
		if (lectureRepository.existsByMemberId(currentMember.getId())) {
			return;
		}
		List<Object> lectureList = requestTimeTable(currentMember.getId(), decrypt(currentMember.getPassword()));
		lectureRepository.saveAll(Lecture.convert(lectureList, currentMember));
	}
}
