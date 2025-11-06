package com.pillmate.pillmate.Service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pillmate.pillmate.Domain.Schedule;
import com.pillmate.pillmate.Domain.ScheduleStatus;
import com.pillmate.pillmate.DTO.ScheduleRequest;
import com.pillmate.pillmate.DTO.ScheduleResponse;
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
}

