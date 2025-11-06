package com.pillmate.pillmate.Repository;

import com.pillmate.pillmate.Domain.IntakeType;
import com.pillmate.pillmate.Domain.UserIntakeSensitivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserIntakeSensitivityRepository
	extends JpaRepository<UserIntakeSensitivity, Long> {

	Optional<UserIntakeSensitivity> findByUserIdAndIntakeType(Long userId, IntakeType intakeType);
}
