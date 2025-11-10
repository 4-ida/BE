package com.pillmate.pillmate.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "복약 일정 수정 요청")
public class ScheduleUpdateRequest {
    
    @Schema(description = "약품 ID (자동완성에서 선택한 약품의 ID)", example = "12")
    private Long drugId;
    
    @Schema(description = "약품명 (자동완성에서 선택한 약품명)", example = "타이레놀정500mg")
    private String name;
    
    @Schema(description = "복용량 또는 용법", example = "1정")
    private String dose;
    
    @Schema(description = "복용 예정 시각 (ISO8601 형식)", example = "2025-10-08T09:00:00")
    private LocalDateTime date;
    
    @Schema(description = "사용자 메모", example = "시간 조정")
    private String memo;
    
    @Schema(description = "계획 상태", example = "SCHEDULED", allowableValues = {"SCHEDULED", "CANCELLED"})
    private String plan;
    
    @Schema(description = "복용 상태", example = "TAKEN", allowableValues = {"TAKEN", "MISSED"})
    private String status;
    
    @Valid
    @Schema(description = "알림 설정")
    private AlarmSettings alarm;
    
    @Schema(description = "복용 시작일", example = "2025-10-08")
    private LocalDate startDate;
    
    @Schema(description = "복용 종료일", example = "2025-10-15")
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

