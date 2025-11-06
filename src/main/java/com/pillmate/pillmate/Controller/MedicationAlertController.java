package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.MedicationCheckRequest;
import com.pillmate.pillmate.DTO.MedicationCheckResponse;
import com.pillmate.pillmate.Service.MedicationAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
	name = "약물 충돌 알림 API",
	description = """
	사용자의 복용 기록을 기반으로  
	약물 간 상호작용 및 중복 복용 여부를 검사하고  
	위험 수준에 따라 알림(Alert)을 생성하는 API입니다.
	"""
)
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class MedicationAlertController {

	private final MedicationAlertService medicationAlertService;

	@Operation(
		summary = "약물 충돌 알림 생성",
		description = """
        사용자의 최근 섭취 기록과 예정된 복용 시간을 기준으로  
        약물 간의 상호작용/중복 복용 여부를 검사하고  
        위험 수준에 따라 알림(Alert)을 생성합니다.
        
        - userId: 검사 대상 사용자 ID  
        - plannedAt: 복용 예정 시간 (ISO 8601 형식)  
        """
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "검사 성공 — 알림 생성 완료"),
		@ApiResponse(responseCode = "400", description = "요청 데이터가 잘못됨"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@PostMapping("/medication-check")
	public ResponseEntity<MedicationCheckResponse> checkMedication(
		@RequestBody MedicationCheckRequest request
	) {
		MedicationCheckResponse response = medicationAlertService.checkMedication(request);
		return ResponseEntity.ok(response);
	}
}

