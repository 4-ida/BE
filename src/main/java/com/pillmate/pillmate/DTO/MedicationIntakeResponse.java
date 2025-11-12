package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "복용 완료 기록 응답")
public class MedicationIntakeResponse {
    
    @Schema(description = "섭취 기록 ID", example = "555")
    private Long intakeId;
    
    @Schema(description = "연동된 일정 ID", example = "101")
    private Long scheduleId;
    
    @Schema(description = "복용 시각", example = "2025-10-08T09:00:00")
    private LocalDateTime takenAt;
    
    @Schema(description = "금지 타이머 계산 결과")
    private List<BanTimerResponse> banTimers;
}



