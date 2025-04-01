package sopio.acha.domain.memberCourse.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberCourse.application.MemberCourseService;
import sopio.acha.domain.memberCourse.presentation.response.MemberCourseListResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member-courses")
@Tag(name = "Member Lecture", description = "사용자 강의 API")
public class MemberCourseController {
	private final MemberCourseService memberCourseService;

	@GetMapping("/today")
	@Operation(summary = "오늘 수강 예정인 강좌 조회 API", description = "사용자가 오늘 수강 예정인 강좌 목록을 조회 합니다.")
	public ResponseEntity<MemberCourseListResponse> getTodayMemberCourse(
		@CurrentMember Member currentMember
	) {
		MemberCourseListResponse response = memberCourseService.getTodayMemberCourse(currentMember);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping
	@Operation(summary = "내 강좌 목록 요일 순 조회 API", description = "이번 학기에 수강하는 강좌를 요일 순으로 목록 조회 합니다.")
	public ResponseEntity<MemberCourseListResponse> getThisSemesterMemberCourse(
		@CurrentMember Member currentMember
	) {
		MemberCourseListResponse response = memberCourseService.getThisSemesterMemberCourse(currentMember);
		return ResponseEntity.ok().body(response);
	}
}
