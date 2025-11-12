package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "활성 타이머 리스트 (카페인/알코올 최신 1개씩)")
public class ActiveTimerListResponse {
    
    @Schema(description = "카페인 활성 타이머 (최신 1개, 없으면 null)")
    private ActiveTimerItem caffeineTimer;
    
    @Schema(description = "알코올 활성 타이머 (최신 1개, 없으면 null)")
    private ActiveTimerItem alcoholTimer;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "활성 타이머 아이템")
    public static class ActiveTimerItem {
        
        @Schema(description = "섭취 기록 ID", example = "1")
        private Long intakeId;
        
        @Schema(description = "섭취 타입", example = "CAFFEINE", allowableValues = {"CAFFEINE", "ALCOHOL"})
        private String intakeType;
        
        @Schema(description = "카페인: 음료명, 알코올: 술 종류명", example = "아메리카노")
        private String name;
        
        @Schema(description = "카페인: 양(mg), 알코올: 양(ml)", example = "150.0")
        private Double amount;
        
        @Schema(description = "알코올 도수(%) - 알코올일 때만", example = "4.5")
        private Double abv;
        
        @Schema(description = "섭취 시각", example = "2025-10-08T18:00:00")
        private java.time.LocalDateTime intakeAt;
        
        @Schema(description = "현재 잔존량 (카페인: mg, 알코올: %BAC)", example = "45.5")
        private Double currentAmount;
        
        @Schema(description = "복약 가능까지 남은 시간(초 단위)", example = "21600")
        private Long remainingSec;
        
        @Schema(description = "복약 가능 예상 시각", example = "2025-10-08T15:00:00")
        private java.time.LocalDateTime expectedSafeTime;
        
        @Schema(description = "이미 복약 가능한지 여부", example = "false")
        private Boolean isSafe;
    }
}


