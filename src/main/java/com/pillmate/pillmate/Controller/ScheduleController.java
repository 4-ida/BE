package com.pillmate.pillmate.Controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pillmate.pillmate.DTO.ScheduleRequest;
import com.pillmate.pillmate.DTO.ScheduleResponse;
import com.pillmate.pillmate.DTO.ScheduleUpdateRequest;
import com.pillmate.pillmate.DTO.ScheduleUpdateResponse;
import com.pillmate.pillmate.Service.ScheduleService;

import java.time.LocalDate;
import java.util.List;

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
    
    @Operation(summary = "복약 일정 조회", description = "단일 날짜 또는 기간으로 복약 일정 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "일정 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/schedules")
    public ResponseEntity<ScheduleListResponse> getSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<ScheduleResponse> schedules;
        
        // date와 from/to 모두 입력된 경우 - 교집합 방식
        if (date != null && from != null && to != null) {
            // date가 from/to 범위 안에 있는지 확인
            if (!date.isBefore(from) && !date.isAfter(to)) {
                // date가 범위 안에 있으면 → date의 일정만 반환
                schedules = scheduleService.getSchedulesByDate(date);
            } else {
                // date가 범위 밖이면 → 에러 반환
                return ResponseEntity.badRequest().body(ScheduleListResponse.builder()
                        .message("date 파라미터는 from과 to 범위 내에 있어야 합니다.")
                        .data(ScheduleListResponse.ScheduleListData.builder()
                                .schedules(java.util.Collections.emptyList())
                                .build())
                        .build());
            }
        } else if (from != null && to != null) {
            // 기간 조회
            schedules = scheduleService.getSchedulesByDateRange(from, to);
        } else if (date != null) {
            // 단일 날짜 조회
            schedules = scheduleService.getSchedulesByDate(date);
        } else if (from != null || to != null) {
            // from 또는 to만 입력한 경우 에러
            return ResponseEntity.badRequest().body(ScheduleListResponse.builder()
                    .message("from과 to 파라미터는 함께 입력해야 합니다.")
                    .data(ScheduleListResponse.ScheduleListData.builder()
                            .schedules(java.util.Collections.emptyList())
                            .build())
                    .build());
        } else {
            // 파라미터 없음 - 400 에러
            return ResponseEntity.badRequest().body(ScheduleListResponse.builder()
                    .message("date 파라미터 또는 from, to 파라미터가 필요합니다.")
                    .data(ScheduleListResponse.ScheduleListData.builder()
                            .schedules(java.util.Collections.emptyList())
                            .build())
                    .build());
        }
        
        ScheduleListResponse response = ScheduleListResponse.builder()
                .message("일정 조회 성공")
                .data(ScheduleListResponse.ScheduleListData.builder()
                        .schedules(schedules)
                        .build())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ScheduleListResponse {
        private String message;
        private ScheduleListData data;
        
        @lombok.Getter
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ScheduleListData {
            private List<ScheduleResponse> schedules;
        }
    }
    
    @Operation(summary = "복약 일정 수정", description = "기존 일정의 세부 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "일정 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleUpdateResponseWrapper> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        ScheduleUpdateResponse updateResponse = scheduleService.updateSchedule(scheduleId, request);
        
        ScheduleUpdateResponseWrapper response = ScheduleUpdateResponseWrapper.builder()
                .message("복약 일정이 수정되었습니다.")
                .data(updateResponse)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "복약 일정 삭제", description = "지정된 복약 일정을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "일정 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleDeleteResponse> deleteSchedule(@PathVariable Long scheduleId) {
        Long deletedScheduleId = scheduleService.deleteSchedule(scheduleId);
        
        ScheduleDeleteResponse response = ScheduleDeleteResponse.builder()
                .message("복약 일정이 삭제되었습니다.")
                .data(ScheduleDeleteResponse.ScheduleDeleteData.builder()
                        .scheduleId(deletedScheduleId)
                        .build())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ScheduleUpdateResponseWrapper {
        private String message;
        private ScheduleUpdateResponse data;
    }
    
    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ScheduleDeleteResponse {
        private String message;
        private ScheduleDeleteData data;
        
        @lombok.Getter
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ScheduleDeleteData {
            private Long scheduleId;
        }
    }
}

