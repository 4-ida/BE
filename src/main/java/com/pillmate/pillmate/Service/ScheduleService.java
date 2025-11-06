package com.pillmate.pillmate.Service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pillmate.pillmate.Domain.Schedule;
import com.pillmate.pillmate.Domain.ScheduleStatus;
import com.pillmate.pillmate.DTO.ScheduleRequest;
import com.pillmate.pillmate.DTO.ScheduleResponse;
import com.pillmate.pillmate.DTO.ScheduleUpdateRequest;
import com.pillmate.pillmate.DTO.ScheduleUpdateResponse;
import com.pillmate.pillmate.Repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    
    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest request) {
        // 복용 기간 검증
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "복용 시작일은 종료일보다 이전이어야 합니다");
        }
        
        // 알림 시각이 복용 기간 내에 있는지 검증
        if (request.getAlarmAt().toLocalDate().isBefore(request.getStartDate()) ||
            request.getAlarmAt().toLocalDate().isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "알림 시각은 복용 기간 내에 있어야 합니다");
        }
        
        // 완전 중복 체크: 같은 약물, 같은 알림 시각, 같은 기간인 경우만 막음
        // (다른 약물을 같은 시간에 복용하는 것은 허용, 같은 약물을 같은 시간에 여러 번 복용하는 것도 허용)
        
        // 약물 충돌 체크 제거: 같은 약물을 여러 번 등록할 수 있도록 허용
        // (필요시 나중에 비즈니스 로직에 따라 추가할 수 있음)
        
        // 일정 생성
        Schedule schedule = Schedule.builder()
                .drugId(request.getDrugId())
                .dose(request.getDose())
                .alarmAt(request.getAlarmAt())
                .memo(request.getMemo())
                .alarmEnabled(request.getAlarm() != null && 
                             request.getAlarm().getEnabled() != null && 
                             request.getAlarm().getEnabled())
                .repeatRule(request.getRepeatRule())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ScheduleStatus.SCHEDULED)
                .build();
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return ScheduleResponse.from(savedSchedule);
    }
    
    /**
     * ID로 단일 일정 조회
     * @param scheduleId 일정 ID
     * @return 일정 정보
     */
    public ScheduleResponse getScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 게시물입니다"));
        
        return ScheduleResponse.from(schedule);
    }
    
    /**
     * 특정 날짜의 일정 조회
     * @param date 조회할 날짜 (YYYY-MM-DD 형식)
     * @return 해당 날짜의 일정 목록
     */
    public List<ScheduleResponse> getSchedulesByDate(LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findByDate(date);
        return schedules.stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 기간의 일정 조회
     * @param from 시작 날짜 (YYYY-MM-DD 형식)
     * @param to 종료 날짜 (YYYY-MM-DD 형식)
     * @return 해당 기간의 일정 목록
     */
    public List<ScheduleResponse> getSchedulesByDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작일은 종료일보다 이전이어야 합니다");
        }
        
        List<Schedule> schedules = scheduleRepository.findByDateRange(from, to);
        return schedules.stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 일정 수정
     * @param scheduleId 일정 ID
     * @param request 수정 요청 데이터
     * @return 수정된 일정 정보와 변경된 필드 리스트
     */
    @Transactional
    public ScheduleUpdateResponse updateSchedule(Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다"));
        
        java.util.List<String> updatedFields = new java.util.ArrayList<>();
        
        // 복용 기간 검증 및 수정
        if (request.getStartDate() != null || request.getEndDate() != null) {
            LocalDate newStartDate = request.getStartDate() != null ? request.getStartDate() : schedule.getStartDate();
            LocalDate newEndDate = request.getEndDate() != null ? request.getEndDate() : schedule.getEndDate();
            
            if (newStartDate.isAfter(newEndDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "복용 시작일은 종료일보다 이전이어야 합니다");
            }
            
            if (request.getStartDate() != null && !request.getStartDate().equals(schedule.getStartDate())) {
                schedule.updateStartDate(request.getStartDate());
                updatedFields.add("startDate");
            }
            
            if (request.getEndDate() != null && !request.getEndDate().equals(schedule.getEndDate())) {
                schedule.updateEndDate(request.getEndDate());
                updatedFields.add("endDate");
            }
        }
        
        // 알림 시각 수정
        if (request.getAlarmAt() != null && !request.getAlarmAt().equals(schedule.getAlarmAt())) {
            LocalDate alarmDate = request.getAlarmAt().toLocalDate();
            LocalDate currentStartDate = request.getStartDate() != null ? request.getStartDate() : schedule.getStartDate();
            LocalDate currentEndDate = request.getEndDate() != null ? request.getEndDate() : schedule.getEndDate();
            
            if (alarmDate.isBefore(currentStartDate) || alarmDate.isAfter(currentEndDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "알림 시각은 복용 기간 내에 있어야 합니다");
            }
            schedule.updateAlarmAt(request.getAlarmAt());
            updatedFields.add("alarmAt");
        }
        
        // 복용량 수정
        if (request.getDose() != null && !request.getDose().equals(schedule.getDose())) {
            schedule.updateDose(request.getDose());
            updatedFields.add("dose");
        }
        
        // 메모 수정
        if (request.getMemo() != null && !java.util.Objects.equals(request.getMemo(), schedule.getMemo())) {
            schedule.updateMemo(request.getMemo());
            updatedFields.add("memo");
        }
        
        // 상태 수정
        if (request.getStatus() != null && !request.getStatus().equals(schedule.getStatus())) {
            // CANCELLED 상태에서는 TAKEN이나 MISSED로 변경 불가
            if (schedule.getStatus() == ScheduleStatus.CANCELLED && 
                (request.getStatus() == ScheduleStatus.TAKEN || request.getStatus() == ScheduleStatus.MISSED)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "취소된 일정은 복용 완료나 미복용 상태로 변경할 수 없습니다");
            }
            schedule.updateStatus(request.getStatus());
            updatedFields.add("status");
        }
        
        // 알림 설정 수정
        if (request.getAlarm() != null && request.getAlarm().getEnabled() != null) {
            boolean newAlarmEnabled = request.getAlarm().getEnabled();
            if (newAlarmEnabled != schedule.getAlarmEnabled()) {
                schedule.updateAlarmEnabled(newAlarmEnabled);
                updatedFields.add("alarm");
            }
        }
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        return ScheduleUpdateResponse.builder()
                .scheduleId(savedSchedule.getScheduleId())
                .updatedFields(updatedFields)
                .status(savedSchedule.getStatus().name())
                .alarmAt(savedSchedule.getAlarmAt())
                .memo(savedSchedule.getMemo())
                .startDate(savedSchedule.getStartDate())
                .endDate(savedSchedule.getEndDate())
                .build();
    }
    
    /**
     * 일정 삭제
     * @param scheduleId 일정 ID
     * @return 삭제된 일정 ID
     */
    @Transactional
    public Long deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다"));
        
        scheduleRepository.delete(schedule);
        return scheduleId;
    }
}

