package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.DashboardProgressResponse;
import com.pillmate.pillmate.Service.DashboardService;
import com.pillmate.pillmate.Util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
		description = "현재 로그인한 사용자의 대시보드 진행률을 조회합니다.\n\n" +
					"year/month 파라미터가 제공되면 해당 월 기준으로 계산하고, 없으면 현재 월 기준으로 계산합니다.\n\n" +
					"**계산 방식**:\n" +
					"- totalPlanned: 해당 기간 내 등록된 일정 개수 (CANCELLED 상태 제외, SCHEDULED/TAKEN/MISSED 포함)\n" +
					"- completed: TAKEN 상태인 일정 개수 (복용 완료)\n" +
					"- missed: MISSED 상태인 일정 개수 (명시적으로 누락으로 표시한 일정)\n" +
					"- progressPercent: (completed / totalPlanned) * 100\n\n" +
					"**참고**: SCHEDULED 상태인 일정은 completed도 missed도 아닌 '예정' 상태입니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 (년도 또는 월 값이 유효하지 않음)"),
		@ApiResponse(responseCode = "401", description = "인증되지 않음")
	})
	@GetMapping("/progress")
	public ResponseEntity<DashboardProgressResponse> getProgress(
		@Parameter(description = "연도 (예: 2025)", required = false)
		@RequestParam(required = false) Integer year,
		@Parameter(description = "월 (1~12)", required = false)
		@RequestParam(required = false) Integer month
	) {
		Long userId = SecurityUtil.currentUserId();
		DashboardProgressResponse response;

		if (year != null && month != null) {
			response = dashboardService.getProgress(userId, year, month);
		} else {
			response = dashboardService.getProgress(userId);
		}

		return ResponseEntity.ok(response);
	}
}
