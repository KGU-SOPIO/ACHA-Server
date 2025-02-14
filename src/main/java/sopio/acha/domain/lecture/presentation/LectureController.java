package sopio.acha.domain.lecture.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.lecture.application.LectureService;
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
}
