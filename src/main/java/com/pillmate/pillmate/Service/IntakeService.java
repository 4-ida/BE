package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.Intake;
import com.pillmate.pillmate.Domain.IntakeType;
import com.pillmate.pillmate.Domain.SensitivityLevel;
import com.pillmate.pillmate.Domain.UserIntakeSensitivity;
import com.pillmate.pillmate.DTO.IntakeRequest;
import com.pillmate.pillmate.DTO.IntakeResponse;
import com.pillmate.pillmate.DTO.SensitivityUpdateRequest;
import com.pillmate.pillmate.DTO.SensitivityUpdateResponse;
import com.pillmate.pillmate.Repository.IntakeRepository;
import com.pillmate.pillmate.Repository.UserIntakeSensitivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntakeService {

	private final IntakeRepository intakeRepository;


	private final UserIntakeSensitivityRepository userIntakeSensitivityRepository;


	private static final DateTimeFormatter RESPONSE_TIME_FORMATTER =
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
		if (list.isEmpty()) {
			throw new IllegalArgumentException("해당 유저의 섭취 로그가 없습니다.");
		}
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
			now.format(RESPONSE_TIME_FORMATTER)
		);
	}

	private double mapHalfLifeHours(IntakeType intakeType, SensitivityLevel level) {

		if (intakeType == IntakeType.CAFFEINE) {
			return switch (level) {
				case WEAK -> 3.0;
				case MEDIUM -> 5.0;
				case STRONG -> 8.0;
			};
		}

		return switch (level) {
			case WEAK -> 3.0;
			case MEDIUM -> 6.0;
			case STRONG -> 9.0;
		};
	}
}
