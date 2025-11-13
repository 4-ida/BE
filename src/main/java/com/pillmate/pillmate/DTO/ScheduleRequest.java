package com.pillmate.pillmate.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

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
    
    @NotNull(message = "복용 날짜는 필수입니다")
    @Schema(description = "복용 날짜 (YYYY-MM-DD 형식, 캘린더에서 선택한 날짜)", example = "2025-10-08", required = true)
    private LocalDate date;
    
    @NotNull(message = "복용 시간은 필수입니다")
    @Schema(description = "복용 시간 (HH:mm 형식)", example = "08:30", required = true)
    private LocalTime time;
    
    @Schema(description = "사용자 메모", example = "식후 30분")
    private String memo;
    
    @Valid
    @Schema(description = "알림 설정")
    private AlarmSettings alarm;
    
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

