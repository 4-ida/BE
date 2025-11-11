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
@Schema(description = "금지 타이머 정보")
public class BanTimerResponse {
    
    @Schema(description = "금지 항목 타입", example = "caffeine", allowableValues = {"caffeine", "alcohol"})
    private String type;
    
    @Schema(description = "보정 계수", example = "2.0")
    private Double adjustmentFactor;
    
    @Schema(description = "남은 금지 시간(초 단위)", example = "21600")
    private Long remainingSec;
    
    @Schema(description = "다음 안전 시간", example = "2025-10-08T15:00:00")
    private LocalDateTime expectedSafeTime;
}


