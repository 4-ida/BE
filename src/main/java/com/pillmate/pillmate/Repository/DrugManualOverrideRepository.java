package com.pillmate.pillmate.Repository;

import com.pillmate.pillmate.Domain.DrugManualOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrugManualOverrideRepository extends JpaRepository<DrugManualOverride, String> {
}


