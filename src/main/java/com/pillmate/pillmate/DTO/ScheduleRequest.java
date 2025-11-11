package com.pillmate.pillmate.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "복약 일정 등록 요청")
public class ScheduleRequest {
    
    @NotNull(message = "약품 ID는 필수입니다")
    @Schema(description = "약품 ID", example = "12", required = true)
    private Long drugId;
    
    @Schema(description = "약품명", example = "타이레놀정500mg")
    private String name;
    
    @NotBlank(message = "복용량은 필수입니다")
    @Schema(description = "복용량 또는 용법", example = "1정", required = true)
    private String dose;
    
    @NotNull(message = "알림 시각은 필수입니다")
    @Schema(description = "복용 예정 시각 (ISO8601 형식)", example = "2025-10-08T08:30:00", required = true)
    private LocalDateTime date;
    
    @Schema(description = "사용자 메모", example = "식후 30분")
    private String memo;
    
    @Valid
    @Schema(description = "알림 설정")
    private AlarmSettings alarm;
    
    @NotNull(message = "복용 시작일은 필수입니다")
    @Schema(description = "복용 시작일", example = "2025-10-08", required = true)
    private LocalDate startDate;
    
    @NotNull(message = "복용 종료일은 필수입니다")
    @Schema(description = "복용 종료일", example = "2025-10-15", required = true)
    private LocalDate endDate;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "알림 설정")
    public static class AlarmSettings {
        @Schema(description = "알림 활성화 여부", example = "true")
        private Boolean enabled;
    }
}

