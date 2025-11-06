package com.pillmate.pillmate.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class IntakeResponse {
	private Long intakeId;
	private Long userId;
	private String beverageName;
	private Double amount;
	private String intakeType;
	private LocalDateTime createdAt;
}
