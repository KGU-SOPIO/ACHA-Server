package sopio.acha.domain.memberLecture.application;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.lecture.domain.Lecture;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.domain.MemberLecture;
import sopio.acha.domain.memberLecture.infrastructure.MemberLectureRepository;

@Service
@RequiredArgsConstructor
public class MemberLectureService {
	private final MemberLectureRepository memberLectureRepository;

	public void saveMyLectures(List<Lecture> lectureList, Member currentMember) {
		List<MemberLecture> memberLectures = lectureList.stream()
			.filter(lecture -> isExistsMemberLecture(currentMember, lecture))
			.map(lecture -> MemberLecture.save(currentMember, lecture))
			.toList();
		memberLectureRepository.saveAll(memberLectures);
	}

	// public MemberLectureHomeListResponse getTodayMemberLecture(Member currentMember) {
	//
	// }

	private boolean isExistsMemberLecture(Member currentMember, Lecture lecture) {
		return !memberLectureRepository.existsByMemberAndLecture(currentMember, lecture);
	}
}
