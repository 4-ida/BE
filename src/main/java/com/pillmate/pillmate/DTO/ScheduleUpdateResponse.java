package com.pillmate.pillmate.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "복약 일정 수정 응답 데이터")
public class ScheduleUpdateResponse {
    
    @Schema(description = "일정 ID", example = "101")
    private Long scheduleId;
    
    @Schema(description = "변경된 필드 리스트", example = "[\"status\", \"alarmAt\"]")
    private List<String> updatedFields;
    
    @Schema(description = "현재 상태", example = "TAKEN")
    private String status;
    
    @Schema(description = "변경된 알림 시각", example = "2025-10-08T09:00:00")
    private LocalDateTime alarmAt;
    
    @Schema(description = "변경된 메모", example = "시간 조정")
    private String memo;
    
    @Schema(description = "복용 시작일", example = "2025-10-08")
    private LocalDate startDate;
    
    @Schema(description = "복용 종료일", example = "2025-10-15")
    private LocalDate endDate;
}

