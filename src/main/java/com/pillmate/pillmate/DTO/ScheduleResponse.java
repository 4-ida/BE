package com.pillmate.pillmate.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

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
    
    @Schema(description = "복용 날짜 (YYYY-MM-DD 형식)", example = "2025-10-08")
    private LocalDate date;
    
    @Schema(description = "복용 시간 (HH:mm 형식)", example = "08:30")
    private LocalTime time;
    
    @Schema(description = "사용자 메모", example = "식후 30분")
    private String memo;
    
    @Schema(description = "계획 상태 (없으면 null)", example = "SCHEDULED", allowableValues = {"SCHEDULED", "CANCELLED"})
    private String plan;
    
    @Schema(description = "복용 상태 (없으면 null. TAKEN/MISSED는 나중에 설정 가능)", example = "TAKEN", allowableValues = {"TAKEN", "MISSED"})
    private String status;
    
    @Schema(description = "알림 설정")
    private AlarmSettings alarm;
    
    @JsonIgnore
    @Schema(description = "내부 일정 상태", hidden = true)
    private ScheduleStatus internalStatus;
    
    /**
     * 조회용: 현재 상태를 그대로 반환
     */
    public static ScheduleResponse from(Schedule schedule) {
        ScheduleStatus currentStatus = schedule.getStatus();
        String resolvedPlan = currentStatus == ScheduleStatus.CANCELLED ? ScheduleStatus.CANCELLED.name() : ScheduleStatus.SCHEDULED.name();
        String resolvedStatus = switch (currentStatus) {
            case TAKEN -> ScheduleStatus.TAKEN.name();
            case MISSED -> ScheduleStatus.MISSED.name();
            default -> null; // SCHEDULED인 경우 status는 null
        };
        
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .drugId(schedule.getDrugId())
                .name(schedule.getDrugName())
                .dose(schedule.getDose())
                .date(schedule.getDate().toLocalDate())
                .time(schedule.getDate().toLocalTime())
                .memo(schedule.getMemo())
                .plan(resolvedPlan)
                .status(resolvedStatus)
                .alarm(AlarmSettings.builder()
                        .enabled(schedule.getAlarmEnabled())
                        .build())
                .internalStatus(currentStatus)
                .build();
    }
    
    /**
     * 등록용: 원본 request의 status와 plan 정보를 기반으로 응답 생성
     * - plan: 기본값 "SCHEDULED" (등록 시 plan이 없으면 DB에 SCHEDULED로 저장, 응답에서도 "SCHEDULED" 반환)
     * - status: 기본값 null (등록 시 status가 없으면 응답에서 null 반환)
     */
    public static ScheduleResponse from(Schedule schedule, String originalStatus, String originalPlan) {
        ScheduleStatus currentStatus = schedule.getStatus();
        
        // plan 처리: 원본 request에 plan이 없었으면 기본값 "SCHEDULED" 반환
        String resolvedPlan;
        if (originalPlan != null && !originalPlan.trim().isEmpty()) {
            // 원본 request에 plan이 있으면 그것 사용
            resolvedPlan = originalPlan.trim().toUpperCase();
        } else {
            // 원본 request에 plan이 없었으면 기본값 "SCHEDULED" 반환
            // DB에는 이미 SCHEDULED로 저장되어 있음
            if (currentStatus == ScheduleStatus.CANCELLED) {
                resolvedPlan = ScheduleStatus.CANCELLED.name();
            } else {
                resolvedPlan = ScheduleStatus.SCHEDULED.name();
            }
        }
        
        // status 처리: 원본 request에 status가 없었으면 null 반환
        String resolvedStatus;
        if (originalStatus != null && !originalStatus.trim().isEmpty()) {
            // 원본 request에 status가 있으면 그것 사용
            resolvedStatus = switch (currentStatus) {
                case TAKEN -> ScheduleStatus.TAKEN.name();
                case MISSED -> ScheduleStatus.MISSED.name();
                default -> null;
            };
        } else {
            // 원본 request에 status가 없었으면 null 반환 (기본값 없음)
            resolvedStatus = null;
        }
        
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .drugId(schedule.getDrugId())
                .name(schedule.getDrugName())
                .dose(schedule.getDose())
                .date(schedule.getDate().toLocalDate())
                .time(schedule.getDate().toLocalTime())
                .memo(schedule.getMemo())
                .plan(resolvedPlan)
                .status(resolvedStatus)
                .alarm(AlarmSettings.builder()
                        .enabled(schedule.getAlarmEnabled())
                        .build())
                .internalStatus(currentStatus)
                .build();
    }
    
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

