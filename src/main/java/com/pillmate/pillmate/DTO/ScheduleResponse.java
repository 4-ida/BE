package com.pillmate.pillmate.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pillmate.pillmate.Domain.Schedule;
import com.pillmate.pillmate.Domain.ScheduleStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "복약 일정 응답")
public class ScheduleResponse {
    
    @Schema(description = "일정 ID", example = "101")
    private Long scheduleId;
    
    @Schema(description = "약품 ID", example = "12")
    private Long drugId;
    
    @Schema(description = "약품명", example = "아모크라정")
    private String drugName;
    
    @Schema(description = "복용량", example = "1정")
    private String dose;
    
    @Schema(description = "알림 시각", example = "2025-10-08T08:30:00")
    private LocalDateTime alarmAt;
    
    @Schema(description = "사용자 메모", example = "식후 30분")
    private String memo;
    
    @Schema(description = "일정 상태", example = "SCHEDULED")
    private ScheduleStatus status;
    
    @Schema(description = "복용 시작일", example = "2025-10-08")
    private LocalDate startDate;
    
    @Schema(description = "복용 종료일", example = "2025-10-15")
    private LocalDate endDate;
    
    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .drugId(schedule.getDrugId())
                .drugName(null)  // TODO: Drug 엔티티와 조인하여 실제 약품명 조회
                .dose(schedule.getDose())
                .alarmAt(schedule.getAlarmAt())
                .memo(schedule.getMemo())
                .status(schedule.getStatus())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .build();
    }
}

