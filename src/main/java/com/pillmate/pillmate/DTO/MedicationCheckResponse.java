package com.pillmate.pillmate.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record MedicationCheckResponse(
	Long alertId,
	Long userId,
	Severity severity,
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	LocalDateTime createdAt
) {
	public enum Severity {
		INFO, WARNING, DANGER
	}
}
