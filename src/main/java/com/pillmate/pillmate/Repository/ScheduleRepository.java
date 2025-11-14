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
    // 같은 약물과 같은 날짜, 같은 알림 시각에 존재하는 일정 조회 (완전 중복 체크용)
    @Query("SELECT s FROM Schedule s WHERE s.drugId = :drugId " +
           "AND s.date = :date " +
           "AND s.alarmAt = :alarmAt")
    List<Schedule> findByDrugIdAndDateAndAlarmAt(@Param("drugId") Long drugId, 
                                                  @Param("date") LocalDate date,
                                                  @Param("alarmAt") LocalDateTime alarmAt);
    
    // 특정 날짜의 일정 조회
    @Query("SELECT s FROM Schedule s WHERE s.date = :date ORDER BY s.alarmAt")
    List<Schedule> findByDate(@Param("date") LocalDate date);
    
    // 특정 기간의 일정 조회 (LocalDate를 받아서 해당 날짜 범위의 모든 일정 조회)
    @Query("SELECT s FROM Schedule s WHERE s.date >= :startDateTime AND s.date < :endDateTime ORDER BY s.date, s.alarmAt")
    List<Schedule> findByDateRange(@Param("startDateTime") LocalDateTime startDateTime,
                                    @Param("endDateTime") LocalDateTime endDateTime);
    
    // 사용자별 일정 개수 조회
    int countByUserId(Long userId);
    
    // 사용자, 약물, 상태로 일정 조회
    List<Schedule> findByUserIdAndDrugIdAndStatus(Long userId, Long drugId, com.pillmate.pillmate.Domain.ScheduleStatus status);
    
    // 사용자, 날짜로 일정 조회 (LocalDate를 받아서 해당 날짜의 모든 일정 조회)
    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND s.date >= :startOfDay AND s.date < :startOfNextDay ORDER BY s.alarmAt")
    List<Schedule> findByUserIdAndDate(@Param("userId") Long userId,
                                        @Param("startOfDay") LocalDateTime startOfDay,
                                        @Param("startOfNextDay") LocalDateTime startOfNextDay);
    
    // 사용자, 기간별 일정 개수 조회
    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.userId = :userId AND s.date >= :startDateTime AND s.date < :endDateTime")
    int countByUserIdAndDateRange(@Param("userId") Long userId,
                                   @Param("startDateTime") LocalDateTime startDateTime,
                                   @Param("endDateTime") LocalDateTime endDateTime);

    // 사용자, 기간별, 상태별 일정 개수 조회
    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.userId = :userId AND s.status = :status AND s.date >= :startDateTime AND s.date < :endDateTime")
    int countByUserIdAndStatusAndDateRange(@Param("userId") Long userId,
                                            @Param("status") com.pillmate.pillmate.Domain.ScheduleStatus status,
                                            @Param("startDateTime") LocalDateTime startDateTime,
                                            @Param("endDateTime") LocalDateTime endDateTime);

    // 사용자, 기간별, 상태가 아닌 일정 개수 조회 (CANCELLED 제외용)
    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.userId = :userId AND s.status != :status AND s.date >= :startDateTime AND s.date < :endDateTime")
    int countByUserIdAndStatusNotAndDateRange(@Param("userId") Long userId,
                                               @Param("status") com.pillmate.pillmate.Domain.ScheduleStatus status,
                                               @Param("startDateTime") LocalDateTime startDateTime,
                                               @Param("endDateTime") LocalDateTime endDateTime);

}