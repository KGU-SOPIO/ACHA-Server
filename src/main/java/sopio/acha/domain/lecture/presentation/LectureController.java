package sopio.acha.domain.lecture.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.lecture.application.LectureService;
import sopio.acha.domain.lecture.presentation.response.LectureSummaryListResponse;
import sopio.acha.domain.lecture.presentation.response.LectureTodayListResponse;
import sopio.acha.domain.member.domain.Member;

/**
 * 홈화면에 보여지는 강의 정보는 Redis를 통해 캐싱 처리
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lecture")
@Tag(name = "Lecture", description = "강의 API")
public class LectureController {
	private final LectureService lectureService;

	@PostMapping
	@Operation(summary = "회원 가입 직후 강좌 정보 스크래핑 요청 API",
		description = "회원 가입 직후, 강좌 정보를 스크래핑하고 해당 데이터를 DB에 저장한 뒤 캐시 서버로 푸시합니다.")
	public ResponseEntity<Void> extractLectureAndSave(
		@CurrentMember Member currentMember
	) {
		lectureService.extractLectureAndSave(currentMember);
		return ResponseEntity.ok().build();
	}

	/*
	@GetMapping
	@Operation(summary = "get today's lecture in home page", description = "홈 화면 오늘의 강좌 정보 불러오기")
	public ResponseEntity<LectureTodayListResponse> getTodayLecture(
		@CurrentMember Member currentMember
	) {
		LectureTodayListResponse response = lectureService.getTodayLecture(currentMember);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/list")
	@Operation(summary = "get my lecture list", description = "나의 강좌 목록 불러오기")
	public ResponseEntity<LectureSummaryListResponse> getLectureList(
		@CurrentMember Member currentMember
	) {
		LectureSummaryListResponse response = lectureService.getAllMyLectureList(currentMember);
		return ResponseEntity.ok().body(response);
	}
*/

}
