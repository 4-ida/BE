package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "위험 약물 보정 결과")
public class MedicationRiskResponse {

	@Schema(description = "유저 ID", example = "1")
	private Long userId;

	@Schema(description = "위험도: LOW / MEDIUM / HIGH", example = "HIGH")
	private String riskLevel;

	@Schema(description = "서버 시간 기준 갱신 시각", example = "2025-10-07:14-05")
	private String updatedAt;
}
