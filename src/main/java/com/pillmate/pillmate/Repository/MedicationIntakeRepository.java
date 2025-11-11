package com.pillmate.pillmate.Repository;

import com.pillmate.pillmate.Domain.MedicationIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationIntakeRepository extends JpaRepository<MedicationIntake, Long> {
    
    List<MedicationIntake> findByScheduleId(Long scheduleId);
    
    List<MedicationIntake> findByDrugId(Long drugId);
}


