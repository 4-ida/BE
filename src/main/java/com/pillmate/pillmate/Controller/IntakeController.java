package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.*;
import com.pillmate.pillmate.Domain.Intake;
import com.pillmate.pillmate.Service.IntakeService;
import com.pillmate.pillmate.Util.SecurityUtil;
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
		description = "카페인 섭취량(mg), 섭취비율(%), 섭취시각으로 카페인 데이터를 등록합니다. JWT 토큰으로 인증된 사용자의 기록으로 저장됩니다.")
	@PostMapping("/caffeine")
	public ResponseEntity<IntakeResponse> createCaffeineIntake(
		@RequestBody CaffeineIntakeRequest request
	) {
		Long userId = SecurityUtil.currentUserId();
		IntakeResponse res = intakeService.createCaffeineIntake(userId, request);
		return ResponseEntity.status(201).body(res);
	}

	// ===========================
	// 3️⃣ 알코올 전용 섭취 등록
	// ===========================
	@Operation(summary = "알코올 섭취 등록",
		description = "술 종류, 기본용량, 잔수, 섭취시각으로 알코올 데이터를 등록합니다. JWT 토큰으로 인증된 사용자의 기록으로 저장됩니다.")
	@PostMapping("/alcohol")
	public ResponseEntity<IntakeResponse> createAlcoholIntake(
		@RequestBody AlcoholIntakeRequest request
	) {
		Long userId = SecurityUtil.currentUserId();
		IntakeResponse res = intakeService.createAlcoholIntake(userId, request);
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

	// ===========================
	// 7️⃣ 카페인 잔존 타이머 조회
	// ===========================
	@Operation(
		summary = "카페인 잔존 타이머 조회",
		description = """
			카페인 섭취 후 약 복용 가능 시간을 계산합니다.

			**계산 로직:**
			- 현재 잔존량(mg) = 초기 섭취량 × (0.5)^(경과시간/반감기)
			- 복약 가능 기준: 30mg 미만
			- 사용자의 카페인 민감도(반감기)와 복용 중인 약물의 보정계수 적용

			**응답 예시 (intakeType: "CAFFEINE"):**
			- currentAmount: 75.5 (현재 75.5mg 잔존)
			- threshold: 30.0 (30mg 미만일 때 복약 가능)
			- halfLifeOrRate: 5.0 (반감기 5시간)
			- adjustmentFactor: 2.0 (항생제 복용 시)
			"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "400", description = "요청 데이터가 잘못됨"),
		@ApiResponse(responseCode = "404", description = "섭취 기록을 찾을 수 없음")
	})
	@GetMapping("/caffeine/{intakeId}/timer")
	public ResponseEntity<ResidualTimerResponse> getCaffeineResidualTimer(
		@PathVariable Long intakeId
	) {
		Long userId = SecurityUtil.currentUserId();
		ResidualTimerResponse response = intakeService.calculateCaffeineResidualTimer(userId, intakeId);
		return ResponseEntity.ok(response);
	}

	// ===========================
	// 8️⃣ 알코올 잔존 타이머 조회
	// ===========================
	@Operation(
		summary = "알코올 잔존 타이머 조회",
		description = """
			알코올 섭취 후 약 복용 가능 시간을 계산합니다.

			**계산 로직:**
			- BAC_peak ≈ 0.02 × 표준잔 수
			- 현재 BAC = max(0, BAC_peak - 대사속도 × 경과시간)
			- 복약 가능 기준: 0.02% BAC 미만
			- 사용자의 음주 패턴(대사속도)과 복용 중인 약물의 보정계수 적용

			**응답 예시 (intakeType: "ALCOHOL"):**
			- currentAmount: 3.4 (현재 0.034% BAC, 백분율로 표시)
			- threshold: 2.0 (0.02% BAC 미만일 때 복약 가능, 백분율로 표시)
			- halfLifeOrRate: 0.013 (대사속도 0.013 %BAC/시간)
			- adjustmentFactor: 1.5 (수면제 복용 시)
			"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "400", description = "요청 데이터가 잘못됨"),
		@ApiResponse(responseCode = "404", description = "섭취 기록을 찾을 수 없음")
	})
	@GetMapping("/alcohol/{intakeId}/timer")
	public ResponseEntity<ResidualTimerResponse> getAlcoholResidualTimer(
		@PathVariable Long intakeId
	) {
		Long userId = SecurityUtil.currentUserId();
		ResidualTimerResponse response = intakeService.calculateAlcoholResidualTimer(userId, intakeId);
		return ResponseEntity.ok(response);
	}

	// ===========================
	// 9️⃣ 활성 타이머 리스트 조회
	// ===========================
	@Operation(
		summary = "활성 타이머 리스트 조회",
		description = """
			사용자의 카페인/알코올 최신 섭취 기록 중 활성 타이머(아직 복약 불가능한 상태)를 조회합니다.

			**조회 로직:**
			- 최신 섭취 기록부터 확인하여 isSafe=false인 첫 번째 기록만 반환
			- 카페인: 잔존량 > 30mg인 경우 활성
			- 알코올: BAC > 0.02%인 경우 활성
			- 각각 최신 1개씩만 반환 (총 최대 2개)

			**응답 구조:**
			- caffeineTimer: 카페인 활성 타이머 (없으면 null)
			- alcoholTimer: 알코올 활성 타이머 (없으면 null)

			각 타이머 객체는 ResidualTimerResponse와 동일한 구조로 intakeType, currentAmount 등 포함
			"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증되지 않음")
	})
	@GetMapping("/active-timers")
	public ResponseEntity<ActiveTimerListResponse> getActiveTimers() {
		Long userId = SecurityUtil.currentUserId();
		ActiveTimerListResponse response = intakeService.getActiveTimers(userId);
		return ResponseEntity.ok(response);
	}
}
