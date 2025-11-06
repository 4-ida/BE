package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.IntakeRequest;
import com.pillmate.pillmate.DTO.IntakeResponse;
import com.pillmate.pillmate.DTO.IntakeResidualResponse;
import com.pillmate.pillmate.DTO.SensitivityUpdateRequest;
import com.pillmate.pillmate.DTO.SensitivityUpdateResponse;
import com.pillmate.pillmate.DTO.MedicationRiskRequest;
import com.pillmate.pillmate.DTO.MedicationRiskResponse;
import com.pillmate.pillmate.Domain.Intake;
import com.pillmate.pillmate.Service.IntakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/intakespage/intakes")
@Tag(name = "Intake", description = "섭취(음료) 기록 등록·조회·수정·삭제 및 부가 계산 API")
public class IntakeController {

	private final IntakeService intakeService;

	// 1️⃣ 섭취 등록
	@Operation(summary = "섭취 등록", description = "사용자가 섭취한 음료/주류 데이터를 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "등록 성공"),
		@ApiResponse(responseCode = "400", description = "요청 데이터가 잘못됨")
	})
	@PostMapping
	public ResponseEntity<IntakeResponse> createIntake(@RequestBody IntakeRequest request) {
		return ResponseEntity.status(201).body(intakeService.create(request));
	}

	// 2️⃣ 섭취 기록 조회
	@Operation(summary = "섭취 기록 조회", description = "특정 사용자(userId)의 섭취 기록 전체를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "해당 사용자의 기록 없음")
	})
	@GetMapping("/{userId}")
	public ResponseEntity<List<Intake>> getIntakesByUser(@PathVariable Long userId) {
		return ResponseEntity.ok(intakeService.getByUser(userId));
	}

	// 3️⃣ 섭취 기록 수정
	@Operation(summary = "섭취 기록 수정", description = "특정 사용자(userId)의 섭취 기록을 수정합니다. (현재는 첫 번째 기록 기준 예시)")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공"),
		@ApiResponse(responseCode = "404", description = "해당 사용자의 기록 없음")
	})
	@PutMapping("/{userId}")
	public ResponseEntity<IntakeResponse> updateIntakeByUser(
		@PathVariable Long userId,
		@RequestBody IntakeRequest request
	) {
		return ResponseEntity.ok(intakeService.updateByUser(userId, request));
	}

	// 4️⃣ 섭취 기록 삭제
	@Operation(summary = "섭취 기록 삭제", description = "특정 사용자(userId)의 섭취 기록을 모두 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "404", description = "해당 사용자의 기록 없음")
	})
	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteIntakeByUser(@PathVariable Long userId) {
		intakeService.deleteByUser(userId);
		return ResponseEntity.noContent().build();
	}

	// 5️⃣ 섭취 민감도(반감기) 설정
	@Operation(summary = "섭취 민감도(반감기) 설정", description = "사용자가 특정 섭취 타입에 대해 민감도를 설정하면 이에 맞는 반감기 시간을 저장합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "설정 성공"),
		@ApiResponse(responseCode = "400", description = "요청 데이터가 잘못됨")
	})
	@PutMapping("/sensitivity")
	public ResponseEntity<SensitivityUpdateResponse> updateSensitivity(
		@RequestBody SensitivityUpdateRequest request
	) {
		return ResponseEntity.ok(intakeService.updateSensitivity(request));
	}

	// 6️⃣ 잔류량 계산
	@Operation(summary = "섭취 잔류량 계산", description = "특정 섭취 로그(intakeId)에 대해 현재 시점 기준으로 남아있는 잔류량을 계산합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "계산 성공",
			content = @Content(schema = @Schema(implementation = IntakeResidualResponse.class))
		),
		@ApiResponse(responseCode = "404", description = "섭취 기록 또는 민감도 설정 없음")
	})
	@GetMapping("/{intakeId}/residual")
	public ResponseEntity<IntakeResidualResponse> getResidual(@PathVariable Long intakeId) {
		return ResponseEntity.ok(intakeService.getResidualByIntakeId(intakeId));
	}

	// 7️⃣ 위험 약물 보정
	@Operation(summary = "위험 약물 보정", description = "사용자가 동시에 섭취하는 타입들을 바탕으로 위험도를 계산합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "보정 성공")
	})
	@PutMapping("/medication-risk")
	public ResponseEntity<MedicationRiskResponse> updateMedicationRisk(
		@RequestBody MedicationRiskRequest request
	) {
		return ResponseEntity.ok(intakeService.updateMedicationRisk(request));
	}
}
