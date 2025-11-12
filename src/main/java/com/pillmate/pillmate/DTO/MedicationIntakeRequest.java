package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "복용 완료 기록 요청")
public class MedicationIntakeRequest {
    
    @NotNull(message = "일정 ID는 필수입니다")
    @Schema(description = "복용한 일정 ID", example = "101", required = true)
    private Long scheduleId;
    
    @NotNull(message = "약품 ID는 필수입니다")
    @Schema(description = "복용한 약품 ID", example = "12", required = true)
    private Long drugId;
    
    @NotNull(message = "복용 시각은 필수입니다")
    @Schema(description = "실제 복용 시각 (ISO8601 형식)", example = "2025-10-08T09:00:00", required = true)
    private LocalDateTime takenAt;
}



