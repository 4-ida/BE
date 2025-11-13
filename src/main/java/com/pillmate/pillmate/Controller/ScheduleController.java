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
import com.pillmate.pillmate.Util.SecurityUtil;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
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
    
    @Operation(summary = "복약 일정 등록", description = "달력에서 선택한 날짜에 새로운 복약 일정을 등록합니다. 단일 날짜만 등록 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "복약 일정 등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/schedules")
    public ResponseEntity<ScheduleCreateResponse> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        Long userId = SecurityUtil.currentUserId();
        ScheduleResponse scheduleResponse = scheduleService.createSchedule(userId, request);
        
        ScheduleCreateResponse response = ScheduleCreateResponse.builder()
                .message("복약 일정이 등록되었습니다.")
                .scheduleId(scheduleResponse.getScheduleId())
                .data(ScheduleCreateResponse.ScheduleData.builder()
                        .drugId(scheduleResponse.getDrugId())
                        .name(scheduleResponse.getName())
                        .dose(scheduleResponse.getDose())
                        .date(scheduleResponse.getDate())
                        .time(scheduleResponse.getTime())
                        .memo(scheduleResponse.getMemo())
                        .plan(scheduleResponse.getPlan())
                        .status(scheduleResponse.getStatus())
                        .alarm(scheduleResponse.getAlarm())
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
        @Schema(description = "생성된 일정 ID", example = "101")
        private Long scheduleId;
        private ScheduleData data;
        
        @lombok.Getter
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ScheduleData {
            private Long drugId;
            private String name;
            private String dose;
            private java.time.LocalDate date;  // 복용 날짜
            private java.time.LocalTime time;  // 복용 시간
            private String memo;
            private String plan;
            private String status;
            private ScheduleResponse.AlarmSettings alarm;
        }
    }
    
    @Operation(
        summary = "복약 일정 목록 조회", 
        description = "동일한 날짜에 등록된 모든 복약 일정(scheduleID)을 리스트로 조회합니다.\n\n" +
                     "**단일 일정 방식**: 각 일정(scheduleID)은 하나의 날짜만 가지며, 독립적으로 관리됩니다.\n" +
                     "동일한 날짜에 여러 일정이 등록되어 있으면 모두 리스트로 반환됩니다.\n\n" +
                     "**응답**: 조회된 날짜와 해당 날짜의 모든 일정 목록을 반환합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "일정 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (날짜 파라미터 누락 또는 잘못된 날짜 형식)")
    })
    @GetMapping("/schedules")
    public ResponseEntity<ScheduleListResponse> getSchedules(
            @RequestParam(required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            @Schema(description = "조회할 날짜 (YYYY-MM-DD 형식, 필수)", example = "2025-11-12", required = true) 
            LocalDate date) {
        Long userId = SecurityUtil.currentUserId();
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDate(userId, date);
        
        ScheduleListResponse response = ScheduleListResponse.builder()
                .message("일정 조회 성공")
                .data(ScheduleListResponse.ScheduleListData.builder()
                        .date(date)
                        .schedules(schedules)
                        .build())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "복약 일정 월별 조회", 
        description = "특정 월에 등록된 모든 복약 일정을 날짜별로 그룹화하여 조회합니다.\n\n" +
                     "**캘린더용 API**: 월별 캘린더 화면에서 사용하기 위한 API입니다.\n\n" +
                     "**응답 형식**: 날짜를 키로 하고, 해당 날짜의 일정 목록을 값으로 하는 맵 형태로 반환됩니다.\n" +
                     "예: `{ \"2025-11-01\": [...], \"2025-11-02\": [...], ... }`"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "일정 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (년도 또는 월 값이 유효하지 않음)")
    })
    @GetMapping("/schedules/month")
    public ResponseEntity<ScheduleMonthResponse> getSchedulesByMonth(
            @RequestParam(required = true) 
            @Schema(description = "조회할 년도", example = "2025", required = true) 
            Integer year,
            @RequestParam(required = true) 
            @Schema(description = "조회할 월 (1-12)", example = "11", required = true) 
            Integer month) {
        Long userId = SecurityUtil.currentUserId();
        java.util.Map<java.time.LocalDate, List<ScheduleResponse>> schedulesByDate = 
                scheduleService.getSchedulesByMonth(userId, year, month);
        
        ScheduleMonthResponse response = ScheduleMonthResponse.builder()
                .message("월별 일정 조회 성공")
                .data(ScheduleMonthResponse.ScheduleMonthData.builder()
                        .year(year)
                        .month(month)
                        .schedulesByDate(schedulesByDate)
                        .build())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "복약 일정 단일 조회", 
        description = "일정 ID를 통해 특정 복약 일정을 조회합니다.\n\n" +
                     "**단일 일정 방식**: 각 일정은 하나의 날짜만 가지며, date 파라미터가 필요 없습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "일정 조회 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 일정입니다")
    })
    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleDetailResponse> getScheduleById(@PathVariable Long scheduleId) {
        ScheduleResponse scheduleResponse = scheduleService.getScheduleById(scheduleId);
        
        ScheduleDetailResponse response = ScheduleDetailResponse.builder()
                .message("일정 조회 성공")
                .data(scheduleResponse)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "복약 일정 목록 조회 응답")
    public static class ScheduleListResponse {
        @Schema(description = "응답 메시지", example = "일정 조회 성공")
        private String message;
        
        @Schema(description = "일정 목록 데이터")
        private ScheduleListData data;
        
        @lombok.Getter
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        @Schema(description = "일정 목록 데이터")
        public static class ScheduleListData {
            @Schema(description = "조회한 날짜", example = "2025-11-12")
            private java.time.LocalDate date;
            
            @Schema(description = "일정 목록 (동일한 날짜에 등록된 모든 scheduleID 목록)")
            private List<ScheduleResponse> schedules;
        }
    }
    
    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "복약 일정 월별 조회 응답")
    public static class ScheduleMonthResponse {
        @Schema(description = "응답 메시지", example = "월별 일정 조회 성공")
        private String message;
        
        @Schema(description = "월별 일정 데이터")
        private ScheduleMonthData data;
        
        @lombok.Getter
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        @Schema(description = "월별 일정 데이터")
        public static class ScheduleMonthData {
            @Schema(description = "조회한 년도", example = "2025")
            private Integer year;
            
            @Schema(description = "조회한 월", example = "11")
            private Integer month;
            
            @Schema(description = "날짜별 일정 목록 (날짜를 키로 하고, 해당 날짜의 일정 목록을 값으로 하는 맵)", 
                     example = "{\"2025-11-01\": [...], \"2025-11-02\": [...]}")
            private java.util.Map<java.time.LocalDate, List<ScheduleResponse>> schedulesByDate;
        }
    }
    
    @Operation(
        summary = "복약 일정 수정", 
        description = "기존 일정의 세부 정보를 수정합니다.\n\n" +
                     "**단일 일정 방식**: 각 일정은 하나의 날짜만 가지며, 모든 필드를 직접 수정할 수 있습니다.\n" +
                     "- `status`를 `TAKEN`으로 변경하면 카페인/알코올 금지 타이머가 활성화됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "일정 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleUpdateResponseWrapper> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        Long userId = SecurityUtil.currentUserId();
        ScheduleUpdateResponse updateResponse = scheduleService.updateSchedule(scheduleId, userId, request);
        
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
        Long userId = SecurityUtil.currentUserId();
        Long deletedScheduleId = scheduleService.deleteSchedule(scheduleId, userId);
        
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
    public static class ScheduleDetailResponse {
        private String message;
        private ScheduleResponse data;
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

