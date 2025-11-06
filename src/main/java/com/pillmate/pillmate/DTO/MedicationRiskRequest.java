package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "위험 약물 보정 요청")
public class MedicationRiskRequest {

	@Schema(description = "유저 ID", example = "1")
	private Long userId;

	@Schema(description = "동시에 고려할 섭취 타입들", example = "[\"CAFFEINE\", \"ALCOHOL\"]")
	private List<String> intakeTypes;
}
