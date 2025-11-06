package com.pillmate.pillmate.Domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
	name = "user_intake_sensitivity",
	uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "intake_type"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIntakeSensitivity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "intake_type", nullable = false)
	private IntakeType intakeType;   // 네가 이미 갖고 있는 enum

	@Enumerated(EnumType.STRING)
	@Column(name = "sensitivity_level", nullable = false)
	private SensitivityLevel sensitivityLevel;

	@Column(name = "half_life_hours", nullable = false)
	private Double halfLifeHours;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
