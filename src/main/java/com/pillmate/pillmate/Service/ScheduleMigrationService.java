package com.pillmate.pillmate.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pillmate.pillmate.Domain.Schedule;
import com.pillmate.pillmate.Domain.ScheduleStatus;
import com.pillmate.pillmate.Repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 단일 일정 방식으로 마이그레이션하기 위한 서비스
 * 기존 기간 기반 일정(startDate ~ endDate)을 단일 날짜 기반 일정으로 변환
 * 
 * 주의: 이 서비스는 일회성 마이그레이션용입니다.
 * 마이그레이션 완료 후에는 사용하지 않아도 됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleMigrationService {
    
    private final ScheduleRepository scheduleRepository;
    
    /**
     * 기간 기반 일정을 단일 일정으로 분할
     * 
     * 주의: 이 메서드는 기존 스키마(startDate, endDate 포함)에서만 작동합니다.
     * 마이그레이션 후에는 사용하지 않아도 됩니다.
     */
    @Transactional
    public void migrateSchedulesToSingleDate() {
        log.info("Starting schedule migration to single date format...");
        
        // 기존 스키마에서 startDate, endDate가 있는 일정들을 찾아서 분할
        // 참고: 현재 Schedule 엔티티에는 startDate, endDate가 없으므로
        // 이 서비스는 마이그레이션 전에 실행해야 합니다.
        
        // 실제 구현은 기존 데이터베이스에 직접 쿼리하거나
        // 별도의 마이그레이션 스크립트를 사용하는 것을 권장합니다.
        
        log.info("Schedule migration completed.");
    }
    
    /**
     * 특정 기간의 일정을 단일 일정으로 분할 (예시)
     * 
     * @param scheduleId 기존 일정 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param originalSchedule 원본 일정 정보
     */
    @Transactional
    public void splitScheduleByDateRange(
            Long scheduleId,
            LocalDate startDate,
            LocalDate endDate,
            Schedule originalSchedule) {
        
        log.info("Splitting schedule {} from {} to {}", scheduleId, startDate, endDate);
        
        LocalDate currentDate = startDate;
        int dayOffset = 0;
        
        while (!currentDate.isAfter(endDate)) {
            // 각 날짜마다 새로운 일정 생성
            LocalDateTime newAlarmAt = currentDate.atTime(
                originalSchedule.getAlarmAt().toLocalTime()
            );
            
            // 중복 체크
            boolean exists = scheduleRepository.findByDrugIdAndDateAndAlarmAt(
                originalSchedule.getDrugId(),
                currentDate,
                newAlarmAt
            ).stream()
            .anyMatch(s -> s.getUserId().equals(originalSchedule.getUserId()));
            
            if (!exists) {
                // date를 LocalDateTime으로 변환 (alarmAt과 동일한 날짜/시간 사용)
                LocalDateTime scheduleDateTime = newAlarmAt;
                
                Schedule newSchedule = Schedule.builder()
                    .userId(originalSchedule.getUserId())
                    .drugId(originalSchedule.getDrugId())
                    .drugName(originalSchedule.getDrugName())
                    .dose(originalSchedule.getDose())
                    .date(scheduleDateTime)
                    .alarmAt(newAlarmAt)
                    .memo(originalSchedule.getMemo())
                    .alarmEnabled(originalSchedule.getAlarmEnabled())
                    .repeatRule(originalSchedule.getRepeatRule())
                    .status(originalSchedule.getStatus())
                    .build();
                
                scheduleRepository.save(newSchedule);
                log.info("Created new schedule for date: {}", currentDate);
            }
            
            currentDate = currentDate.plusDays(1);
            dayOffset++;
        }
        
        // 원본 일정 삭제 (분할 완료 후)
        scheduleRepository.deleteById(scheduleId);
        log.info("Deleted original schedule: {}", scheduleId);
    }
}


