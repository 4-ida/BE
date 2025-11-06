package com.pillmate.pillmate.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SensitivityUpdateRequest {
	private Long userId;
	private String intakeType;       // "CAFFEINE"
	private String sensitivityLevel; // "WEAK"
}
