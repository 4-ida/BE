package com.pillmate.pillmate.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record MedicationCheckRequest(
	Long userId,
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	LocalDateTime plannedAt
) {}
