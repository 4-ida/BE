package com.pillmate.pillmate.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    
    @Schema(description = "약품명", example = "타이레놀정500mg")
    private String name;
    
    @Schema(description = "복용량", example = "1정")
    private String dose;
    
    @Schema(description = "복용 예정 시각", example = "2025-10-08T08:30:00")
    private LocalDateTime date;
    
    @Schema(description = "사용자 메모", example = "식후 30분")
    private String memo;
    
    @Schema(description = "계획 상태", example = "SCHEDULED", allowableValues = {"SCHEDULED", "CANCELLED"})
    private String plan;
    
    @Schema(description = "복용 상태", example = "TAKEN", allowableValues = {"TAKEN", "MISSED"})
    private String status;
    
    @JsonIgnore
    @Schema(description = "내부 일정 상태", hidden = true)
    private ScheduleStatus internalStatus;
    
    @Schema(description = "복용 시작일", example = "2025-10-08")
    private LocalDate startDate;
    
    @Schema(description = "복용 종료일", example = "2025-10-15")
    private LocalDate endDate;
    
    public static ScheduleResponse from(Schedule schedule) {
        ScheduleStatus currentStatus = schedule.getStatus();
        String resolvedPlan = currentStatus == ScheduleStatus.CANCELLED ? ScheduleStatus.CANCELLED.name() : ScheduleStatus.SCHEDULED.name();
        String resolvedStatus = switch (currentStatus) {
            case TAKEN -> ScheduleStatus.TAKEN.name();
            case MISSED -> ScheduleStatus.MISSED.name();
            default -> null;
        };
        
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .drugId(schedule.getDrugId())
                .name(schedule.getDrugName())
                .dose(schedule.getDose())
                .date(schedule.getAlarmAt())
                .memo(schedule.getMemo())
                .plan(resolvedPlan)
                .status(resolvedStatus)
                .internalStatus(currentStatus)
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .build();
    }
}

