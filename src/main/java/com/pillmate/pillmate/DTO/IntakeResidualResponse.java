package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;

@Getter
@AllArgsConstructor
@Schema(description = "섭취 로그에 대한 잔류량 계산 결과 DTO")
public class IntakeResidualResponse {

	@Schema(description = "섭취 로그 ID", example = "1")
	private Long intakeId;

	@Schema(description = "섭취한 타입 (예: CAFFEINE, ALCOHOL 등)", example = "CAFFEINE")
	private String intakeType;

	@Schema(description = "처음 섭취한 양 (ml 또는 mg 기준)", example = "200.0")
	private Double originalAmount;

	@Schema(description = "현재 시점 기준 계산된 잔류 양", example = "120.5")
	private Double remainingAmount;

	@Schema(description = "효과가 거의 0이 되는 예상 시각(서버 기준)", example = "2025-11-07:23-10")
	private String estimatedZeroAt;

	@Schema(description = "계산에 사용된 가정값들 (반감기, 경과시간 등)", example = "{\"halfLifeHours\": 6.0, \"hoursPassed\": 2.5}")
	private Map<String, Object> assumptions;

	@Schema(description = "서버 기준 현재 시각", example = "2025-11-07:14-05")
	private String now;
}

