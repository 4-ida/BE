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
@Schema(description = "금지 타이머 정보 (약 복용 후 카페인/알코올 섭취 금지 시간)")
public class BanTimerResponse {

    @Schema(
        description = "금지 항목 타입",
        allowableValues = {"caffeine", "alcohol"},
        example = "caffeine"
    )
    private String type;

    @Schema(
        description = "약물군 보정 계수 (기본 시간에 곱해짐). 예: 항생제 ×2.0, 수면제/진정제 ×1.5",
        example = "2.0",
        implementation = Double.class
    )
    private Double adjustmentFactor;

    @Schema(
        description = "남은 금지 시간 (초 단위). 예: 카페인(항생제) 43200초(12시간), 알코올(수면제) 37800초(10.5시간)",
        example = "43200",
        implementation = Long.class
    )
    private Long remainingSec;

    @Schema(
        description = "섭취 가능 예상 시각 (금지 시간 이후)",
        example = "2025-11-16T08:00:00",
        implementation = LocalDateTime.class
    )
    private LocalDateTime expectedSafeTime;
}


