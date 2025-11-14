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

    @Schema(description = "섭취 타입", example = "CAFFEINE", allowableValues = {"CAFFEINE", "ALCOHOL"})
    private String intakeType;

    @Schema(description = "현재 잔존량 (카페인: mg, 알코올: %BAC)", example = "45.5")
    private Double currentAmount;

    @Schema(description = "복약 가능 기준값 (카페인: 30mg, 알코올: 0.02%)", example = "30.0")
    private Double threshold;

    @Schema(description = "반감기 또는 대사 속도 (카페인: 시간 단위, 알코올: %BAC/시간)", example = "5.0")
    private Double halfLifeOrRate;

    @Schema(description = "섭취 후 경과 시간(시간 단위)", example = "2.5")
    private Double hoursPassed;

    @Schema(description = "약물군 보정 계수 (회피 시간에 곱해짐)", example = "2.0")
    private Double adjustmentFactor;

    @Schema(description = "복약 가능 예상 시각", example = "2025-10-08T15:00:00")
    private LocalDateTime expectedSafeTime;

    @Schema(description = "복약 가능까지 남은 시간(초 단위)", example = "21600")
    private Long remainingSec;

    @Schema(description = "이미 복약 가능한지 여부", example = "false")
    private Boolean isSafe;

    @Schema(description = "계산에 사용된 가정값들 (반감기, 대사속도 등)", example = "{\"halfLifeHours\": 5.0, \"hoursPassed\": 2.5}")
    private java.util.Map<String, Object> assumptions;
}


