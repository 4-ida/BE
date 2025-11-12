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

}
