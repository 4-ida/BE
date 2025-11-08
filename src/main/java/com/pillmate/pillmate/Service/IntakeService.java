package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.Intake;
import com.pillmate.pillmate.Domain.IntakeType;
import com.pillmate.pillmate.DTO.IntakeRequest;
import com.pillmate.pillmate.DTO.CaffeineIntakeRequest;
import com.pillmate.pillmate.DTO.AlcoholIntakeRequest;
import com.pillmate.pillmate.DTO.IntakeResponse;
import com.pillmate.pillmate.Repository.IntakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntakeService {

	private final IntakeRepository intakeRepository;

	public IntakeResponse create(IntakeRequest req) {
		Intake intake = Intake.builder()
			.userId(req.getUserId())
			.beverageName(req.getBeverageName())
			.amount(req.getAmount())
			.intakeType(IntakeType.valueOf(req.getIntakeType())) // 문자열 -> Enum
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
		// 단순하게 첫 번째 로그만 수정하는 예시
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
	// ===========================
	// 카페인 전용 섭취 등록
	// ===========================
	public IntakeResponse createCaffeineIntake(CaffeineIntakeRequest req) {

		double finalAmount = req.getCaffeineMg();
		if (req.getIntakeRatio() != null) {
			finalAmount *= (req.getIntakeRatio() / 100.0);
		}

		Intake intake = Intake.builder()
			.userId(req.getUserId())
			.beverageName(req.getBeverageName())
			.amount(finalAmount)
			.intakeType(IntakeType.CAFFEINE)   // ← 고정 상수로 지정
			.createdAt(req.getIntakeAt() != null ? req.getIntakeAt() : LocalDateTime.now())
			.build();

		intakeRepository.save(intake);

		// getIntakeType() 대신, 그냥 우리가 넣은 값 그대로 전달
		return new IntakeResponse(
			intake.getIntakeId(),
			intake.getUserId(),
			intake.getBeverageName(),
			intake.getAmount(),
			IntakeType.CAFFEINE.name(),
			intake.getCreatedAt()
		);

	}

	// ===========================
	// 2️⃣ 알코올 전용 섭취 등록
	// ===========================
	public IntakeResponse createAlcoholIntake(AlcoholIntakeRequest req) {

		double finalAmount = req.getVolumeMl() != null ? req.getVolumeMl() : 0.0;

		Intake intake = Intake.builder()
			.userId(req.getUserId())
			.beverageName(req.getAlcoholType())
			.amount(finalAmount)
			.intakeType(IntakeType.ALCOHOL)
			.createdAt(req.getIntakeAt() != null ? req.getIntakeAt() : LocalDateTime.now())
			.build();

		intakeRepository.save(intake);

		return new IntakeResponse(
			intake.getIntakeId(),
			intake.getUserId(),
			intake.getBeverageName(),
			intake.getAmount(),
			IntakeType.CAFFEINE.name(),
			intake.getCreatedAt()
		);

	}




}
