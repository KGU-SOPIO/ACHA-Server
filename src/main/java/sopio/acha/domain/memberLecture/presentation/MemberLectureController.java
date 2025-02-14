package sopio.acha.domain.memberLecture.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberLecture.application.MemberLectureService;
import sopio.acha.domain.memberLecture.presentation.response.MemberLectureHomeListResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member-lecture")
@Tag(name = "Member Lecture", description = "사용자 강의 API")
public class MemberLectureController {
	private final MemberLectureService memberLectureService;

	// @GetMapping("/today")
	// @Operation(summary = "오늘 수강 예정인 강의 조회 API", description = "사용자가 오늘 수강 예정인 강의 목록을 조회 합니다.")
	// public ResponseEntity<MemberLectureHomeListResponse> getTodayMemberLecture(
	// 	@CurrentMember Member currentMember
	// ) {
	// 	MemberLectureHomeListResponse response = memberLectureService.getTodayMemberLecture(currentMember);
	// 	return ResponseEntity.ok().body(response);
	// }
}
