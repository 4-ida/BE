package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.*;
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
@Tag(name = "Intake", description = "섭취(음료/주류) 기록 등록·조회·수정·삭제 API")
public class IntakeController {

	private final IntakeService intakeService;

	// ===========================
	// 1️⃣ 기본 섭취 등록 (공통)
	// ===========================
	@Operation(hidden = true)	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "등록 성공"),
		@ApiResponse(responseCode = "400", description = "요청 데이터가 잘못됨")
	})
	@PostMapping
	public ResponseEntity<IntakeResponse> createIntake(@RequestBody IntakeRequest request) {
		IntakeResponse res = intakeService.create(request);
		return ResponseEntity.status(201).body(res);
	}

	// ===========================
	// 2️⃣ 카페인 전용 섭취 등록
	// ===========================
	@Operation(summary = "카페인 섭취 등록",
		description = "카페인 섭취량(mg), 섭취비율(%), 섭취시각으로 카페인 데이터를 등록합니다.")
	@PostMapping("/caffeine")
	public ResponseEntity<IntakeResponse> createCaffeineIntake(
		@RequestBody CaffeineIntakeRequest request
	) {
		IntakeResponse res = intakeService.createCaffeineIntake(request);
		return ResponseEntity.status(201).body(res);
	}

	// ===========================
	// 3️⃣ 알코올 전용 섭취 등록
	// ===========================
	@Operation(summary = "알코올 섭취 등록",
		description = "술 종류, 기본용량, 잔수, 섭취시각으로 알코올 데이터를 등록합니다.")
	@PostMapping("/alcohol")
	public ResponseEntity<IntakeResponse> createAlcoholIntake(
		@RequestBody AlcoholIntakeRequest request
	) {
		IntakeResponse res = intakeService.createAlcoholIntake(request);
		return ResponseEntity.status(201).body(res);
	}

	// ===========================
	// 4️⃣ 사용자별 섭취 기록 조회
	// ===========================
	@Operation(hidden = true)	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "해당 사용자의 기록 없음")
	})
	@GetMapping("/{userId}")
	public ResponseEntity<List<Intake>> getIntakesByUser(@PathVariable Long userId) {
		return ResponseEntity.ok(intakeService.getByUser(userId));
	}

	// ===========================
	// 5️⃣ 섭취 기록 수정
	// ===========================
	@Operation(hidden = true)	@PutMapping("/{userId}")
	public ResponseEntity<IntakeResponse> updateIntakeByUser(
		@PathVariable Long userId,
		@RequestBody IntakeRequest request
	) {
		return ResponseEntity.ok(intakeService.updateByUser(userId, request));
	}

	// ===========================
	// 6️⃣ 섭취 기록 삭제
	// ===========================
	@Operation(hidden = true)	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteIntakeByUser(@PathVariable Long userId) {
		intakeService.deleteByUser(userId);
		return ResponseEntity.noContent().build();
	}
}
