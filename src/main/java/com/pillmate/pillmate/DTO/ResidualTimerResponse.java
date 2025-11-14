package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "잔존 타이머 정보 (카페인/알코올 섭취 후 약 복용 가능 시간)")
public class ResidualTimerResponse {

    @Schema(
        description = "섭취 타입",
        allowableValues = {"CAFFEINE", "ALCOHOL"},
        example = "CAFFEINE"
    )
    private String intakeType;

    @Schema(
        description = "현재 잔존량. 카페인: mg 단위 (예: 75.5mg), 알코올: %BAC 백분율 (예: 3.4는 0.034%BAC)",
        example = "75.5",
        implementation = Double.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double currentAmount;

    @Schema(
        description = "복약 가능 기준값. 카페인: 30mg 미만, 알코올: 2.0 (0.02%BAC 미만)",
        example = "30.0",
        implementation = Double.class
    )
    private Double threshold;

    @Schema(
        description = "반감기 또는 대사 속도. 카페인: 반감기 시간 (예: 5.0시간), 알코올: %BAC/시간 대사속도 (예: 0.013)",
        example = "5.0",
        implementation = Double.class
    )
    private Double halfLifeOrRate;

    @Schema(
        description = "섭취 후 경과 시간 (시간 단위)",
        example = "2.5",
        implementation = Double.class
    )
    private Double hoursPassed;

    @Schema(
        description = "약물군 보정 계수 (최종 회피 시간에 곱해짐). 예: 항생제 2.0, 수면제/진정제 1.5",
        example = "2.0",
        implementation = Double.class
    )
    private Double adjustmentFactor;

    @Schema(
        description = "복약 가능 예상 시각",
        example = "2025-11-16T06:00:00",
        implementation = LocalDateTime.class
    )
    private LocalDateTime expectedSafeTime;

    @Schema(
        description = "복약 가능까지 남은 시간 (초 단위). 예: 6시간 = 21600초",
        example = "21600",
        implementation = Long.class
    )
    private Long remainingSec;

    @Schema(
        description = "이미 복약 가능한지 여부 (true: 복약 가능, false: 복약 불가)",
        example = "false",
        implementation = Boolean.class
    )
    private Boolean isSafe;

    @Schema(
        description = "계산에 사용된 상세 가정값들",
        example = "{\"halfLifeHours\": 5.0, \"hoursPassed\": 2.5, \"initialMg\": 150.0, \"adjustmentFactor\": 2.0}"
    )
    private java.util.Map<String, Object> assumptions;
}


