package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.IntakeRequest;
import com.pillmate.pillmate.DTO.IntakeResponse;
import com.pillmate.pillmate.DTO.SensitivityUpdateRequest;
import com.pillmate.pillmate.DTO.SensitivityUpdateResponse;
import com.pillmate.pillmate.Domain.Intake;
import com.pillmate.pillmate.Service.IntakeService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Intake", description = "섭취(음료) 기록 등록·조회·수정·삭제 API")
public class IntakeController {

	private final IntakeService intakeService;

	@Operation(summary = "섭취 등록", description = "사용자가 섭취한 음료/주류 데이터를 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "등록 성공"),
		@ApiResponse(responseCode = "400", description = "요청 데이터가 잘못됨")
	})
	@PostMapping
	public ResponseEntity<IntakeResponse> createIntake(@RequestBody IntakeRequest request) {
		IntakeResponse res = intakeService.create(request);
		return ResponseEntity.status(201).body(res);
	}

	@Operation(summary = "섭취 등록 조회", description = "특정 사용자(userId)의 섭취 기록 전체를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "해당 사용자의 기록 없음")
	})
	@GetMapping("/{userId}")
	public ResponseEntity<List<Intake>> getIntakesByUser(@PathVariable Long userId) {
		return ResponseEntity.ok(intakeService.getByUser(userId));
	}

	@Operation(summary = "섭취 등록 수정", description = "특정 사용자(userId)의 섭취 기록을 수정합니다. (현재는 첫 번째 기록 기준으로 예시 구현)")
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

	@Operation(summary = "섭취 등록 삭제", description = "특정 사용자(userId)의 섭취 기록을 모두 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "404", description = "해당 사용자의 기록 없음")
	})
	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteIntakeByUser(@PathVariable Long userId) {
		intakeService.deleteByUser(userId);
		return ResponseEntity.noContent().build();
	}
	@Operation(summary = "섭취 민감도(반감기) 설정", description = "사용자가 특정 섭취 타입에 대해 민감도(WEAK/MEDIUM/STRONG)를 설정하면, 이에 맞는 반감기 시간을 저장합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "설정 성공"),
		@ApiResponse(responseCode = "400", description = "요청 데이터가 잘못됨")
	})
	@PutMapping("/sensitivity")
	public ResponseEntity<SensitivityUpdateResponse> updateSensitivity(
		@RequestBody SensitivityUpdateRequest request
	) {
		SensitivityUpdateResponse res = intakeService.updateSensitivity(request);
		return ResponseEntity.ok(res);
	}

}
