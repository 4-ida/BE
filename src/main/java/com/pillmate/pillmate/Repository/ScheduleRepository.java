package com.pillmate.pillmate.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pillmate.pillmate.Domain.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    // 같은 약물과 같은 알림 시각에 존재하는 일정 조회 (완전 중복 체크용)
    @Query("SELECT s FROM Schedule s WHERE s.drugId = :drugId " +
           "AND s.alarmAt = :alarmAt")
    List<Schedule> findByDrugIdAndAlarmAt(@Param("drugId") Long drugId, 
                                           @Param("alarmAt") LocalDateTime alarmAt);
    
    // 특정 기간에 약물이 이미 등록되어 있는지 확인 (약물 충돌 체크용)
    @Query("SELECT s FROM Schedule s WHERE s.drugId = :drugId " +
           "AND ((s.startDate <= :endDate AND s.endDate >= :startDate))")
    List<Schedule> findOverlappingSchedules(@Param("drugId") Long drugId, 
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
    
    // 특정 기간의 일정 조회
    @Query("SELECT s FROM Schedule s WHERE s.startDate <= :endDate AND s.endDate >= :startDate ORDER BY s.alarmAt")
    List<Schedule> findByDateRange(@Param("startDate") LocalDate startDate, 
                                    @Param("endDate") LocalDate endDate);
    
    // 특정 날짜의 일정 조회 (해당 날짜가 startDate와 endDate 사이에 있는 일정)
    @Query("SELECT s FROM Schedule s WHERE s.startDate <= :date AND s.endDate >= :date ORDER BY s.alarmAt")
    List<Schedule> findByDate(@Param("date") LocalDate date);
}

