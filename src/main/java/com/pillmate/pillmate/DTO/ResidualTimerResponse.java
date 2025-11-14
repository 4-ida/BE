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
        description = "현재 잔존량",
        example = "75.5",
        implementation = Double.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @io.swagger.v3.oas.annotations.media.ExampleObject(
        name = "카페인 예시",
        value = "75.5",
        description = "카페인: mg 단위 (예: 75.5mg)"
    )
    @io.swagger.v3.oas.annotations.media.ExampleObject(
        name = "알코올 예시",
        value = "3.4",
        description = "알코올: %BAC 백분율 (예: 3.4는 0.034%BAC)"
    )
    private Double currentAmount;

    @Schema(
        description = "복약 가능 기준값",
        example = "30.0",
        implementation = Double.class
    )
    @io.swagger.v3.oas.annotations.media.ExampleObject(
        name = "카페인 기준",
        value = "30.0",
        description = "카페인: 30mg 미만"
    )
    @io.swagger.v3.oas.annotations.media.ExampleObject(
        name = "알코올 기준",
        value = "2.0",
        description = "알코올: 2.0 (0.02%BAC 미만)"
    )
    private Double threshold;

    @Schema(
        description = "반감기 또는 대사 속도",
        example = "5.0",
        implementation = Double.class
    )
    @io.swagger.v3.oas.annotations.media.ExampleObject(
        name = "카페인 반감기",
        value = "5.0",
        description = "카페인: 반감기 시간 (예: 5.0시간)"
    )
    @io.swagger.v3.oas.annotations.media.ExampleObject(
        name = "알코올 대사속도",
        value = "0.013",
        description = "알코올: %BAC/시간 대사속도 (예: 0.013)"
    )
    private Double halfLifeOrRate;

    @Schema(
        description = "섭취 후 경과 시간 (시간 단위)",
        example = "2.5",
        implementation = Double.class
    )
    private Double hoursPassed;

    @Schema(
        description = "약물군 보정 계수 (최종 회피 시간에 곱해짐)",
        example = "2.0",
        implementation = Double.class
    )
    @io.swagger.v3.oas.annotations.media.ExampleObject(
        name = "항생제",
        value = "2.0",
        description = "항생제 복용 시"
    )
    @io.swagger.v3.oas.annotations.media.ExampleObject(
        name = "수면제",
        value = "1.5",
        description = "수면제/진정제 복용 시"
    )
    private Double adjustmentFactor;

    @Schema(
        description = "복약 가능 예상 시각",
        example = "2025-11-16T06:00:00",
        implementation = LocalDateTime.class
    )
    private LocalDateTime expectedSafeTime;

    @Schema(
        description = "복약 가능까지 남은 시간 (초 단위)",
        example = "21600",
        implementation = Long.class
    )
    @io.swagger.v3.oas.annotations.media.ExampleObject(
        name = "6시간",
        value = "21600",
        description = "6시간 = 21600초"
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


