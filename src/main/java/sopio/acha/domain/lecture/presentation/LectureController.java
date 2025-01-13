package sopio.acha.domain.lecture.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.domain.lecture.application.LectureService;
import sopio.acha.domain.lecture.presentation.response.LectureSummaryListResponse;

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
	public ResponseEntity<Void> storeLecture() {
		lectureService.saveLecture();
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<LectureSummaryListResponse> getTodayLecture() {
		LectureSummaryListResponse response = lectureService.getTodayLecture();
		return ResponseEntity.ok().body(response);
	}
}
