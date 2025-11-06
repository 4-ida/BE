package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.MedicationCheckRequest;
import com.pillmate.pillmate.DTO.MedicationCheckResponse;
import com.pillmate.pillmate.Service.MedicationAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class MedicationAlertController {

	private final MedicationAlertService medicationAlertService;

	@PostMapping("/medication-check")
	public ResponseEntity<MedicationCheckResponse> checkMedication(
		@RequestBody MedicationCheckRequest request
	) {
		MedicationCheckResponse response = medicationAlertService.checkMedication(request);
		return ResponseEntity.ok(response);
	}
}
