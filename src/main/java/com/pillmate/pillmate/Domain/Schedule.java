package com.pillmate.pillmate.Domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schedules")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Schedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;
    
    @Column(nullable = false)
    private Long userId;  // 사용자 ID
    
    @Column(nullable = false)
    private Long drugId;  // 약품 ID

    @Column(length = 150)
    private String drugName; // 약품명 (자동완성 결과)
    
    @Column(nullable = false, length = 100)
    private String dose;  // 복용량 또는 용법
    
    @Column(nullable = false)
    private LocalDateTime date;  // 복용 날짜 및 시각
    
    @Column(nullable = false)
    private LocalDateTime alarmAt;  // 복용 예정 시각
    
    @Column
    private LocalDate startDate;  // 복용 기간 시작일 (표시용)
    
    @Column
    private LocalDate endDate;  // 복용 기간 종료일 (표시용)
    
    @Column(columnDefinition = "TEXT")
    private String memo;  // 사용자 메모
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean alarmEnabled;  // 알림 사용 여부
    
    @Column(length = 500)
    private String repeatRule;  // 반복 규칙 (RFC5545 형식)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'SCHEDULED'")
    private ScheduleStatus status;  // 일정 상태
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 일정 수정을 위한 메서드들
    public void updateAlarmAt(LocalDateTime alarmAt) {
        this.alarmAt = alarmAt;
    }
    
    public void updateDose(String dose) {
        this.dose = dose;
    }
    
    public void updateMemo(String memo) {
        this.memo = memo;
    }
    
    public void updateStatus(ScheduleStatus status) {
        this.status = status;
    }
    
    public void updateAlarmEnabled(Boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }
    
    public void updateDate(LocalDateTime date) {
        this.date = date;
    }
    
    public void updateStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public void updateEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }
    
    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }
}

