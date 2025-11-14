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
    
    @Operation(
        summary = "카페인 금지 타이머 조회",
        description = """
            약 복용 후 카페인 섭취 금지 시간을 조회합니다.

            **계산 로직:**
            - 최종 금지시간 = 기본 시간(6시간) × 보정계수
            - TAKEN 상태인 일정만 조회 가능
            - 현재 날짜의 SCHEDULED 약물 중 가장 높은 보정계수 적용

            **응답 예시 (type: "caffeine"):**
            - adjustmentFactor: 2.0 (항생제 복용 시)
            - remainingSec: 43200 (6시간 × 2.0 = 12시간 = 43200초)
            - expectedSafeTime: 복약 후 12시간 뒤 시각

            **주의:** TAKEN 상태가 아니면 204 No Content 반환
            """
    )
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
    
    @Operation(
        summary = "알코올 금지 타이머 조회",
        description = """
            약 복용 후 알코올 섭취 금지 시간을 조회합니다.

            **계산 로직:**
            - 최종 금지시간 = 기본 시간(7시간) × 보정계수
            - TAKEN 상태인 일정만 조회 가능
            - 현재 날짜의 SCHEDULED 약물 중 가장 높은 보정계수 적용

            **응답 예시 (type: "alcohol"):**
            - adjustmentFactor: 1.5 (수면제 복용 시)
            - remainingSec: 37800 (7시간 × 1.5 = 10.5시간 = 37800초)
            - expectedSafeTime: 복약 후 10.5시간 뒤 시각

            **주의:** TAKEN 상태가 아니면 204 No Content 반환
            """
    )
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
