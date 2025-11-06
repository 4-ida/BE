package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.DTO.*;
import com.pillmate.pillmate.Domain.*;
import com.pillmate.pillmate.Repository.IntakeRepository;
import com.pillmate.pillmate.Repository.UserIntakeSensitivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IntakeService {

	private final IntakeRepository intakeRepository;
	private final UserIntakeSensitivityRepository userIntakeSensitivityRepository;

	private static final DateTimeFormatter TIME_FMT =
		DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm");

	public IntakeResponse create(IntakeRequest req) {
		Intake intake = Intake.builder()
			.userId(req.getUserId())
			.beverageName(req.getBeverageName())
			.amount(req.getAmount())
			.intakeType(IntakeType.valueOf(req.getIntakeType()))
			.createdAt(LocalDateTime.now())
			.build();

		Intake saved = intakeRepository.save(intake);

		return new IntakeResponse(
			saved.getIntakeId(),
			saved.getUserId(),
			saved.getBeverageName(),
			saved.getAmount(),
			saved.getIntakeType().name(),
			saved.getCreatedAt()
		);
	}

	public List<Intake> getByUser(Long userId) {
		return intakeRepository.findByUserId(userId);
	}

	public IntakeResponse updateByUser(Long userId, IntakeRequest req) {
		List<Intake> list = intakeRepository.findByUserId(userId);
		if (list.isEmpty()) throw new IllegalArgumentException("해당 유저의 섭취 로그가 없습니다.");

		Intake intake = list.get(0);
		intake.setBeverageName(req.getBeverageName());
		intake.setAmount(req.getAmount());
		intake.setIntakeType(IntakeType.valueOf(req.getIntakeType()));

		Intake updated = intakeRepository.save(intake);

		return new IntakeResponse(
			updated.getIntakeId(),
			updated.getUserId(),
			updated.getBeverageName(),
			updated.getAmount(),
			updated.getIntakeType().name(),
			updated.getCreatedAt()
		);
	}

	public void deleteByUser(Long userId) {
		intakeRepository.deleteByUserId(userId);
	}

	public SensitivityUpdateResponse updateSensitivity(SensitivityUpdateRequest req) {
		IntakeType intakeType = IntakeType.valueOf(req.getIntakeType());
		SensitivityLevel level = SensitivityLevel.valueOf(req.getSensitivityLevel());

		double halfLife = mapHalfLifeHours(intakeType, level);
		LocalDateTime now = LocalDateTime.now();

		UserIntakeSensitivity entity = userIntakeSensitivityRepository
			.findByUserIdAndIntakeType(req.getUserId(), intakeType)
			.orElse(UserIntakeSensitivity.builder()
				.userId(req.getUserId())
				.intakeType(intakeType)
				.build());

		entity.setSensitivityLevel(level);
		entity.setHalfLifeHours(halfLife);
		entity.setUpdatedAt(now);

		UserIntakeSensitivity saved = userIntakeSensitivityRepository.save(entity);

		return new SensitivityUpdateResponse(
			saved.getUserId(),
			saved.getIntakeType().name(),
			saved.getSensitivityLevel().name(),
			saved.getHalfLifeHours(),
			now.format(TIME_FMT)
		);
	}

	private double mapHalfLifeHours(IntakeType intakeType, SensitivityLevel level) {
		if (intakeType == IntakeType.CAFFEINE) {
			return switch (level) {
				case WEAK -> 3.0;
				case MEDIUM -> 5.0;
				case STRONG -> 8.0;
			};
		} else {
			return switch (level) {
				case WEAK -> 3.0;
				case MEDIUM -> 6.0;
				case STRONG -> 9.0;
			};
		}
	}

	public IntakeResidualResponse getResidualByIntakeId(Long intakeId) {
		Intake intake = intakeRepository.findById(intakeId)
			.orElseThrow(() -> new IllegalArgumentException("섭취 기록을 찾을 수 없습니다."));

		Long userId = intake.getUserId();
		IntakeType intakeType = intake.getIntakeType();

		UserIntakeSensitivity sensitivity = userIntakeSensitivityRepository
			.findByUserIdAndIntakeType(userId, intakeType)
			.orElseThrow(() -> new IllegalStateException("민감도 설정이 없습니다."));

		double halfLife = sensitivity.getHalfLifeHours();
		double original = intake.getAmount();

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime created = intake.getCreatedAt();

		double hoursPassed = Duration.between(created, now).toMinutes() / 60.0;
		double remaining = original * Math.pow(0.5, hoursPassed / halfLife);

		LocalDateTime estimatedZeroAt = created.plusHours((long) (halfLife * 5));

		Map<String, Object> assumptions = new HashMap<>();
		assumptions.put("halfLifeHours", halfLife);
		assumptions.put("hoursPassed", hoursPassed);

		return new IntakeResidualResponse(
			intake.getIntakeId(),
			intakeType.name(),
			original,
			remaining,
			estimatedZeroAt.format(TIME_FMT),
			assumptions,
			now.format(TIME_FMT)
		);
	}

	public MedicationRiskResponse updateMedicationRisk(MedicationRiskRequest req) {
		if (req.getIntakeTypes() == null || req.getIntakeTypes().isEmpty()) {
			return new MedicationRiskResponse(req.getUserId(), "LOW", LocalDateTime.now().format(TIME_FMT));
		}

		boolean hasCaffeine = req.getIntakeTypes().contains("CAFFEINE");
		boolean hasAlcohol = req.getIntakeTypes().contains("ALCOHOL");

		String risk = (hasCaffeine && hasAlcohol) ? "HIGH"
			: (hasAlcohol ? "MEDIUM" : "LOW");

		return new MedicationRiskResponse(req.getUserId(), risk, LocalDateTime.now().format(TIME_FMT));
	}
}
