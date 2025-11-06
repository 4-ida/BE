package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
@Schema(description = "섭취 로그에 대한 잔류량 계산 결과")
public class IntakeResidualResponse {

	@Schema(description = "섭취 로그 ID")
	private Long intakeId;

	@Schema(description = "섭취한 타입 (예: CAFFEINE)")
	private String intakeType;

	@Schema(description = "처음 섭취한 양", example = "200.0")
	private Double originalAmount;

	@Schema(description = "현재 시점 기준 계산된 잔류 양", example = "120.5")
	private Double remainingAmount;

	@Schema(description = "효과가 거의 0이 된다고 보는 시각(서버 기준)", example = "2025-10-07:23-10")
	private String estimatedZeroAt;

	@Schema(description = "계산에 사용된 가정값들 (반감기, 배수, 경과시간 등)")
	private Map<String, Object> assumptions;

	@Schema(description = "서버가 계산한 시각", example = "2025-10-07:14-05")
	private String now;
}
