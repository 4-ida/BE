package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.DTO.BanTimerResponse;
import com.pillmate.pillmate.DTO.MedicationIntakeRequest;
import com.pillmate.pillmate.DTO.MedicationIntakeResponse;
import com.pillmate.pillmate.Domain.*;
import com.pillmate.pillmate.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicationIntakeService {
    
    private final ScheduleRepository scheduleRepository;
    private final MedicationIntakeRepository medicationIntakeRepository;
    private final DrugClassificationService drugClassificationService;
    
    // 금지 타이머 기본 시간 상수
    private static final int CAFFEINE_BASE_BAN_HOURS = 6; // 카페인 기본 금지 시간 6시간
    private static final int ALCOHOL_BASE_BAN_HOURS = 7; // 알코올 기본 금지 시간 7시간
    
    @Transactional
    public MedicationIntakeResponse recordIntake(MedicationIntakeRequest request) {
        // 일정 존재 확인
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다"));
        
        // 일정의 약품 ID와 요청의 약품 ID 일치 확인
        if (!schedule.getDrugId().equals(request.getDrugId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "일정의 약품 ID와 요청의 약품 ID가 일치하지 않습니다");
        }
        
        // 일정 상태를 TAKEN으로 변경
        schedule.updateStatus(ScheduleStatus.TAKEN);
        scheduleRepository.save(schedule);
        
        // 복용 기록 저장
        MedicationIntake medicationIntake = MedicationIntake.builder()
                .scheduleId(request.getScheduleId())
                .drugId(request.getDrugId())
                .takenAt(request.getTakenAt())
                .build();
        
        MedicationIntake savedIntake = medicationIntakeRepository.save(medicationIntake);
        
        // 금지 타이머 계산 (기본 시간 × 보정 계수)
        // TAKEN 상태로 변경되면 카페인 6시간, 알코올 7시간 × 보정 계수만큼 금지
        List<BanTimerResponse> banTimers = calculateBanTimers(
                request.getTakenAt(), 
                request.getDrugId()
        );
        
        return MedicationIntakeResponse.builder()
                .intakeId(savedIntake.getIntakeId())
                .scheduleId(request.getScheduleId())
                .takenAt(request.getTakenAt())
                .banTimers(banTimers)
                .build();
    }
    
    /**
     * 금지 타이머 계산
     * 기본 시간 × 보정 계수 방식으로 계산
     * TAKEN 상태로 변경되면 활성화되는 금지 타이머
     * 
     * @param takenAt 복용 시각
     * @param drugId 약품 ID (약물군 보정 계수 조회용)
     * @return 금지 타이머 리스트 (카페인, 알코올)
     */
    private List<BanTimerResponse> calculateBanTimers(
            LocalDateTime takenAt, 
            Long drugId) {
        return calculateBanTimersInternal(takenAt, drugId);
    }
    
    /**
     * 금지 타이머 계산 (public 메서드)
     * 기본 시간 × 보정 계수 방식으로 계산
     * TAKEN 상태로 변경되면 활성화되는 금지 타이머
     * 
     * @param takenAt 복용 시각
     * @param drugId 약품 ID (약물군 보정 계수 조회용)
     * @return 금지 타이머 리스트 (카페인, 알코올)
     */
    public List<BanTimerResponse> calculateBanTimersInternal(
            LocalDateTime takenAt, 
            Long drugId) {
        
        List<BanTimerResponse> banTimers = new ArrayList<>();
        
        // 약물군 보정 계수 조회 (카페인과 알코올 동일한 계수 사용)
        String drugIdString = String.valueOf(drugId);
        double adjustmentFactor = drugClassificationService.getCaffeineAdjustmentFactor(drugIdString);
        
        // 현재 시간 기준으로 남은 금지 시간 계산
        LocalDateTime now = LocalDateTime.now();
        
        // 카페인 금지 타이머 계산: 기본 시간(6시간) × 보정 계수
        long caffeineBanSeconds = (long) (CAFFEINE_BASE_BAN_HOURS * 3600 * adjustmentFactor);
        LocalDateTime caffeineSafeTime = takenAt.plusSeconds(caffeineBanSeconds);
        long caffeineRemainingSeconds = java.time.Duration.between(now, caffeineSafeTime).getSeconds();
        
        // 이미 금지 시간이 지났으면 0으로 설정
        if (caffeineRemainingSeconds < 0) {
            caffeineRemainingSeconds = 0;
        }
        
        banTimers.add(BanTimerResponse.builder()
                .type("caffeine")
                .adjustmentFactor(adjustmentFactor)
                .remainingSec(caffeineRemainingSeconds)
                .expectedSafeTime(caffeineSafeTime)
                .build());
        
        // 알코올 금지 타이머 계산: 기본 시간(7시간) × 보정 계수
        long alcoholBanSeconds = (long) (ALCOHOL_BASE_BAN_HOURS * 3600 * adjustmentFactor);
        LocalDateTime alcoholSafeTime = takenAt.plusSeconds(alcoholBanSeconds);
        long alcoholRemainingSeconds = java.time.Duration.between(now, alcoholSafeTime).getSeconds();
        
        // 이미 금지 시간이 지났으면 0으로 설정
        if (alcoholRemainingSeconds < 0) {
            alcoholRemainingSeconds = 0;
        }
        
        banTimers.add(BanTimerResponse.builder()
                .type("alcohol")
                .adjustmentFactor(adjustmentFactor)
                .remainingSec(alcoholRemainingSeconds)
                .expectedSafeTime(alcoholSafeTime)
                .build());
        
        return banTimers;
    }
    
    /**
     * 카페인 금지 타이머 계산 (기본 시간 × 보정 계수)
     * TAKEN 상태로 변경되면 활성화되는 금지 타이머
     * 
     * @param takenAt 복용 시각
     * @param drugId 약품 ID (약물군 보정 계수 조회용)
     * @return 카페인 금지 타이머
     */
    public BanTimerResponse calculateCaffeineBanTimer(LocalDateTime takenAt, Long drugId) {
        String drugIdString = String.valueOf(drugId);
        double adjustmentFactor = drugClassificationService.getCaffeineAdjustmentFactor(drugIdString);
        
        // 기본 시간(6시간) × 보정 계수
        long banSeconds = (long) (CAFFEINE_BASE_BAN_HOURS * 3600 * adjustmentFactor);
        LocalDateTime safeTime = takenAt.plusSeconds(banSeconds);
        
        return BanTimerResponse.builder()
                .type("caffeine")
                .adjustmentFactor(adjustmentFactor)
                .remainingSec(banSeconds)
                .expectedSafeTime(safeTime)
                .build();
    }
    
    /**
     * 알코올 금지 타이머 계산 (기본 시간 × 보정 계수)
     * TAKEN 상태로 변경되면 활성화되는 금지 타이머
     * 
     * @param takenAt 복용 시각
     * @param drugId 약품 ID (약물군 보정 계수 조회용)
     * @return 알코올 금지 타이머
     */
    public BanTimerResponse calculateAlcoholBanTimer(LocalDateTime takenAt, Long drugId) {
        String drugIdString = String.valueOf(drugId);
        double adjustmentFactor = drugClassificationService.getCaffeineAdjustmentFactor(drugIdString);
        
        // 기본 시간(7시간) × 보정 계수
        long banSeconds = (long) (ALCOHOL_BASE_BAN_HOURS * 3600 * adjustmentFactor);
        LocalDateTime safeTime = takenAt.plusSeconds(banSeconds);
        
        return BanTimerResponse.builder()
                .type("alcohol")
                .adjustmentFactor(adjustmentFactor)
                .remainingSec(banSeconds)
                .expectedSafeTime(safeTime)
                .build();
    }
    
    /**
     * 카페인 금지 타이머 조회
     * 특정 일정(scheduleId)에 대한 카페인 금지 타이머를 조회
     * 해당 일정의 복용 기록을 기반으로 남은 금지 시간을 계산
     */
    public BanTimerResponse getCaffeineBanTimer(Long userId, Long scheduleId) {
        // 일정 조회 및 사용자 확인
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다"));
        
        // 사용자 확인
        if (!schedule.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 일정만 조회할 수 있습니다");
        }
        
        // 일정이 TAKEN 상태가 아니면 null 반환
        if (!schedule.getStatus().equals(ScheduleStatus.TAKEN)) {
            return null;
        }
        
        // 해당 일정의 복용 기록 조회
        List<MedicationIntake> intakes = medicationIntakeRepository.findByScheduleId(scheduleId);
        if (intakes.isEmpty()) {
            // 복용 기록이 없으면 null 반환 (204 No Content)
            return null;
        }
        
        // 가장 최근 복용 기록 찾기
        MedicationIntake latestIntake = intakes.stream()
                .max((a, b) -> a.getTakenAt().compareTo(b.getTakenAt()))
                .orElse(null);
        
        if (latestIntake == null) {
            return null;
        }
        
        LocalDateTime takenAt = latestIntake.getTakenAt();
        Long drugId = schedule.getDrugId();
        
        // 약물군 보정 계수 조회
        String drugIdString = String.valueOf(drugId);
        double adjustmentFactor = drugClassificationService.getCaffeineAdjustmentFactor(drugIdString);
        
        // 기본 금지 시간(6시간) × 보정 계수
        long totalBanSeconds = (long) (CAFFEINE_BASE_BAN_HOURS * 3600 * adjustmentFactor);
        LocalDateTime expectedSafeTime = takenAt.plusSeconds(totalBanSeconds);
        
        // 현재 시간 기준 남은 시간 계산
        LocalDateTime now = LocalDateTime.now();
        long remainingSeconds = Duration.between(now, expectedSafeTime).getSeconds();
        
        // 이미 금지 시간이 지났으면 0으로 설정
        if (remainingSeconds < 0) {
            remainingSeconds = 0;
        }
        
        return BanTimerResponse.builder()
                .type("caffeine")
                .adjustmentFactor(adjustmentFactor)
                .remainingSec(remainingSeconds)
                .expectedSafeTime(expectedSafeTime)
                .build();
    }
    
    /**
     * 알코올 금지 타이머 조회
     * 특정 일정(scheduleId)에 대한 알코올 금지 타이머를 조회
     * 해당 일정의 복용 기록을 기반으로 남은 금지 시간을 계산
     */
    public BanTimerResponse getAlcoholBanTimer(Long userId, Long scheduleId) {
        // 일정 조회 및 사용자 확인
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다"));
        
        // 사용자 확인
        if (!schedule.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 일정만 조회할 수 있습니다");
        }
        
        // 일정이 TAKEN 상태가 아니면 null 반환
        if (!schedule.getStatus().equals(ScheduleStatus.TAKEN)) {
            return null;
        }
        
        // 해당 일정의 복용 기록 조회
        List<MedicationIntake> intakes = medicationIntakeRepository.findByScheduleId(scheduleId);
        if (intakes.isEmpty()) {
            // 복용 기록이 없으면 null 반환 (204 No Content)
            return null;
        }
        
        // 가장 최근 복용 기록 찾기
        MedicationIntake latestIntake = intakes.stream()
                .max((a, b) -> a.getTakenAt().compareTo(b.getTakenAt()))
                .orElse(null);
        
        if (latestIntake == null) {
            return null;
        }
        
        LocalDateTime takenAt = latestIntake.getTakenAt();
        Long drugId = schedule.getDrugId();
        
        // 약물군 보정 계수 조회
        String drugIdString = String.valueOf(drugId);
        double adjustmentFactor = drugClassificationService.getCaffeineAdjustmentFactor(drugIdString);
        
        // 기본 금지 시간(7시간) × 보정 계수
        long totalBanSeconds = (long) (ALCOHOL_BASE_BAN_HOURS * 3600 * adjustmentFactor);
        LocalDateTime expectedSafeTime = takenAt.plusSeconds(totalBanSeconds);
        
        // 현재 시간 기준 남은 시간 계산
        LocalDateTime now = LocalDateTime.now();
        long remainingSeconds = Duration.between(now, expectedSafeTime).getSeconds();
        
        // 이미 금지 시간이 지났으면 0으로 설정
        if (remainingSeconds < 0) {
            remainingSeconds = 0;
        }
        
        return BanTimerResponse.builder()
                .type("alcohol")
                .adjustmentFactor(adjustmentFactor)
                .remainingSec(remainingSeconds)
                .expectedSafeTime(expectedSafeTime)
                .build();
    }
}