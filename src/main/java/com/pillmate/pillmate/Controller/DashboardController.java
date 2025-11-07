package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.DashboardProgressResponse;
import com.pillmate.pillmate.Service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
	name = "대시보드 진행률 API",
	description = "사용자가 기간을 설정해둔 상태에서 캘린더에 체크된 복용 현황을 집계해 진행률을 반환합니다."
)
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@Operation(
		summary = "대시보드 진행률 조회",
		description = "사용자의 설정된 기간을 기준으로 총 계획 대비 실제 복용/체크 횟수를 계산하여 진행률을 반환합니다."
	)
	@GetMapping("/progress")
	public ResponseEntity<DashboardProgressResponse> getProgress(@RequestParam Long userId) {
		DashboardProgressResponse response = dashboardService.getProgress(userId);
		return ResponseEntity.ok(response);
	}
}
