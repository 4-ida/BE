package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.DTO.MedicationCheckRequest;
import com.pillmate.pillmate.DTO.MedicationCheckResponse;
import com.pillmate.pillmate.DTO.MedicationCheckResponse.Severity;
import com.pillmate.pillmate.Repository.IntakeRepository;
import com.pillmate.pillmate.Domain.Intake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class MedicationAlertService {

	private final IntakeRepository intakeRepository;

	// 데모용 alertId 발생기 (DB 엔티티 만들면 이거 빼면 됨)
	private static final AtomicLong ALERT_ID_SEQ = new AtomicLong(9000);

	public MedicationCheckResponse checkMedication(MedicationCheckRequest req) {

		LocalDateTime 기준시간 = req.plannedAt() != null
			? req.plannedAt()
			: LocalDateTime.now();

		// 1) 이 사용자의 최근 섭취 가져오기 (48시간 정도 범위 예시)
		//    아래 메서드는 Repository에 우리가 하나 추가해줄 거야.
		List<Intake> recentIntakes =
			intakeRepository.findByUserIdAndCreatedAtAfter(
				req.userId(),
				기준시간.minusHours(48)
			);

		// 2) 위험도 계산 (지금은 예시로 무조건 WARNING)
		Severity severity = evaluateRisk(recentIntakes, 기준시간);

		// 3) 응답 생성 (네 문서 형식)
		Long newAlertId = ALERT_ID_SEQ.incrementAndGet();
		LocalDateTime now = LocalDateTime.now();

		return new MedicationCheckResponse(
			newAlertId,
			req.userId(),
			severity,
			now
		);
	}

	private Severity evaluateRisk(List<Intake> recentIntakes, LocalDateTime 기준시간) {
		// TODO: 여기다가 네가 이미 만든 “잔류량 계산/타이머/반감기 설정” 로직을 재사용
		// 예시: 아무것도 없으면 INFO, 뭔가 있으면 WARNING
		if (recentIntakes == null || recentIntakes.isEmpty()) {
			return Severity.INFO;
		}
		return Severity.WARNING;
	}
}
