package sopio.acha.domain.memberLecture.application;

import static sopio.acha.common.handler.DateHandler.getTodayDate;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.handler.DateHandler;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.lecture.domain.LectureDay;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.domain.MemberLecture;
import sopio.acha.domain.memberLecture.infrastructure.MemberLectureRepository;
import sopio.acha.domain.memberLecture.presentation.response.MemberLectureListResponse;

@Service
@RequiredArgsConstructor
public class MemberLectureService {
	private final MemberLectureRepository memberLectureRepository;

	public void saveMyLectures(List<Lecture> lectureHasTimeTable, Member currentMember) {
		List<MemberLecture> memberLectures = lectureHasTimeTable.stream()
			.filter(lecture -> isExistsMemberLecture(currentMember, lecture))
			.map(lecture -> new MemberLecture(currentMember, lecture))
			.toList();
		memberLectureRepository.saveAll(memberLectures);
	}

	@Transactional(readOnly = true)
	public MemberLectureListResponse getTodayMemberLecture(Member currentMember) {
		LectureDay today = LectureDay.valueOf(getTodayDate());
		List<MemberLecture> memberLectures = memberLectureRepository.findAllByMemberIdAndLectureDayAndLectureYearAndLectureSemester(
			currentMember.getId(), today, DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester());
		return MemberLectureListResponse.from(memberLectures);
	}

	@Transactional(readOnly = true)
	public MemberLectureListResponse getThisSemesterMemberLecture(Member currentMember) {
		List<MemberLecture> memberLectures = memberLectureRepository.findAllByMemberIdAndLectureYearAndLectureSemesterOrderByLectureDayOrderAsc(
			currentMember.getId(), DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester());
		return MemberLectureListResponse.from(memberLectures);
	}

	@Transactional
	public List<MemberLecture> getCurrentMemberLectureAndSetLastUpdatedAt(Member currentMember) {
		return memberLectureRepository.findAllByMemberIdAndLectureYearAndLectureSemesterOrderByLectureDayOrderAsc(
				currentMember.getId(), DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester())
			.stream()
			.peek(MemberLecture::setLastUpdatedAt)
			.toList();
	}

	@Transactional
	public List<MemberLecture> getAllMemberLecture() {
		return memberLectureRepository.findAllByLectureYearAndLectureSemesterOrderByLectureDayOrderAsc(
			DateHandler.getCurrentSemesterYear(), DateHandler.getCurrentSemester())
			.stream()
			.peek(MemberLecture::setLastUpdatedAt)
			.toList();
	}

	private boolean isExistsMemberLecture(Member currentMember, Lecture lecture) {
		return !memberLectureRepository.existsByMemberAndLecture(currentMember, lecture);
	}
}
