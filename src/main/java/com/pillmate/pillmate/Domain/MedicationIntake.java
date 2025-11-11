package com.pillmate.pillmate.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_intakes")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MedicationIntake {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long intakeId;
    
    @Column(nullable = false)
    private Long scheduleId;  // 연동된 일정 ID
    
    @Column(nullable = false)
    private Long drugId;  // 복용한 약품 ID
    
    @Column(nullable = false)
    private LocalDateTime takenAt;  // 실제 복용 시각
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}


