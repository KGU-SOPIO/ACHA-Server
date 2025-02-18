package sopio.acha.domain.memberLecture.application;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.domain.MemberLecture;
import sopio.acha.domain.memberLecture.infrastructure.MemberLectureRepository;
import sopio.acha.domain.memberLecture.presentation.response.MemberLectureHomeListResponse;

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

	public MemberLectureHomeListResponse getTodayMemberLecture(Member currentMember) {

	}

	private boolean isExistsMemberLecture(Member currentMember, Lecture lecture) {
		return !memberLectureRepository.existsByMemberAndLecture(currentMember, lecture);
	}
}
