package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.BanTimerResponse;
import com.pillmate.pillmate.Service.MedicationIntakeService;
import com.pillmate.pillmate.Util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/main/medication")
@RequiredArgsConstructor
@Tag(name = "금지 타이머", description = "카페인/알코올 금지 타이머 조회 API")
public class MedicationIntakeController {
    
    private final MedicationIntakeService medicationIntakeService;
    
    @Operation(summary = "카페인 금지 타이머 조회", 
        description = "특정 일정(scheduleId)에 대한 카페인 금지 타이머를 조회합니다. 해당 일정의 약물에 대한 약물군 보정 계수가 적용됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음"),
        @ApiResponse(responseCode = "204", description = "TAKEN 상태의 복용 기록이 없음")
    })
    @GetMapping("/timer/caffeine")
    public ResponseEntity<BanTimerResponse> getCaffeineBanTimer(@RequestParam Long scheduleId) {
        Long userId = SecurityUtil.currentUserId();
        BanTimerResponse timer = medicationIntakeService.getCaffeineBanTimer(userId, scheduleId);
        
        if (timer == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(timer);
    }
    
    @Operation(summary = "알코올 금지 타이머 조회", 
        description = "특정 일정(scheduleId)에 대한 알코올 금지 타이머를 조회합니다. 해당 일정의 약물에 대한 약물군 보정 계수가 적용됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음"),
        @ApiResponse(responseCode = "204", description = "TAKEN 상태의 복용 기록이 없음")
    })
    @GetMapping("/timer/alcohol")
    public ResponseEntity<BanTimerResponse> getAlcoholBanTimer(@RequestParam Long scheduleId) {
        Long userId = SecurityUtil.currentUserId();
        BanTimerResponse timer = medicationIntakeService.getAlcoholBanTimer(userId, scheduleId);
        
        if (timer == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(timer);
    }
}
