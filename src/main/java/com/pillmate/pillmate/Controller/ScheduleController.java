package com.pillmate.pillmate.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pillmate.pillmate.DTO.ScheduleRequest;
import com.pillmate.pillmate.DTO.ScheduleResponse;
import com.pillmate.pillmate.Service.ScheduleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/main/calendar")
@RequiredArgsConstructor
@Tag(name = "캘린더 일정", description = "캘린더 복약 일정 관련 API")
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    @Operation(summary = "복약 일정 등록", description = "메인 캘린더에 새로운 복약 일정을 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "복약 일정 등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "409", description = "알림 시각 중복 또는 약물 충돌")
    })
    @PostMapping("/schedules")
    public ResponseEntity<ScheduleCreateResponse> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse scheduleResponse = scheduleService.createSchedule(request);
        
        ScheduleCreateResponse response = ScheduleCreateResponse.builder()
                .message("복약 일정이 등록되었습니다.")
                .data(ScheduleCreateResponse.ScheduleData.builder()
                        .scheduleId(scheduleResponse.getScheduleId())
                        .drugId(scheduleResponse.getDrugId())
                        .dose(scheduleResponse.getDose())
                        .alarmAt(scheduleResponse.getAlarmAt())
                        .status(scheduleResponse.getStatus().name())
                        .startDate(scheduleResponse.getStartDate())
                        .endDate(scheduleResponse.getEndDate())
                        .build())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ScheduleCreateResponse {
        private String message;
        private ScheduleData data;
        
        @lombok.Getter
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ScheduleData {
            private Long scheduleId;
            private Long drugId;
            private String dose;
            private java.time.LocalDateTime alarmAt;
            private String status;
            private java.time.LocalDate startDate;  // 복용 시작일
            private java.time.LocalDate endDate;    // 복용 종료일
        }
    }
}

