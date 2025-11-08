package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.DashboardProgressResponse;
import com.pillmate.pillmate.Service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
		description = "userId는 필수, year/month 주면 그 달 기준으로 계산. 안 주면 이번 달 기준."
	)
	@GetMapping("/progress")
	public ResponseEntity<DashboardProgressResponse> getProgress(
		@RequestParam Long userId,
		@Parameter(description = "연도 (예: 2025)", required = false)
		@RequestParam(required = false) Integer year,
		@Parameter(description = "월 (1~12)", required = false)
		@RequestParam(required = false) Integer month
	) {
		DashboardProgressResponse response;

		if (year != null && month != null) {
			response = dashboardService.getProgress(userId, year, month);
		} else {
			response = dashboardService.getProgress(userId);
		}

		return ResponseEntity.ok(response);
	}
}
