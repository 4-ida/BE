package com.pillmate.pillmate.Repository;

import com.pillmate.pillmate.Domain.Intake;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface IntakeRepository extends JpaRepository<Intake, Long> {
	List<Intake> findByUserId(Long userId);
	void deleteByUserId(Long userId);
	int countByUserId(Long userId);

}
