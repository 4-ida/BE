package com.pillmate.pillmate.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SensitivityUpdateResponse {
	private Long userId;
	private String intakeType;
	private String sensitivityLevel;
	private Double halfLifeHours;
	private String updatedAt;
}
