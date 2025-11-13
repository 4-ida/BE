package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.*;
import com.pillmate.pillmate.DTO.*;
import com.pillmate.pillmate.Repository.IntakeRepository;
import com.pillmate.pillmate.Repository.ScheduleRepository;
import com.pillmate.pillmate.Repository.UserRepository;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntakeService {

	private final IntakeRepository intakeRepository;
	private final UserRepository userRepository;
	private final ScheduleRepository scheduleRepository;
	private final DrugClassificationService drugClassificationService;

	// 알코올 기본 용량 및 도수 상수
	private static final double BEER_VOLUME_ML = 500.0;
	private static final double BEER_ABV = 4.5;
	private static final double SOJU_VOLUME_ML = 360.0;
	private static final double SOJU_ABV = 17.0;
	private static final double WINE_VOLUME_ML = 150.0;
	private static final double WINE_ABV = 12.0;
	private static final double WHISKEY_VOLUME_ML = 45.0;
	private static final double WHISKEY_ABV = 40.0;
	
	// 에탄올 밀도 (g/mL)
	private static final double ETHANOL_DENSITY = 0.789;
	
	// 한국 기준 1 표준잔 = 14g 순수 알코올
	private static final double STANDARD_DRINK_ALCOHOL_G = 14.0;
	
	// 카페인 복약 가능 기준 (mg)
	private static final double CAFFEINE_THRESHOLD_MG = 30.0;
	
	// 알코올 복약 가능 기준 (%BAC)
	private static final double ALCOHOL_THRESHOLD_BAC = 0.02;

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
	public IntakeResponse createCaffeineIntake(Long userId, CaffeineIntakeRequest req) {

		double finalAmount = req.getCaffeineMg();
		if (req.getIntakeRatio() != null) {
			finalAmount *= (req.getIntakeRatio() / 100.0);
		}

		LocalDateTime intakeAt = resolveIntakeAt(
			req.getIntakeAt(),
			req.getMeridiem(),
			req.getHour(),
			req.getMinute()
		);

		Intake intake = Intake.builder()
			.userId(userId)  // JWT 토큰에서 가져온 userId 사용
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
	public IntakeResponse createAlcoholIntake(Long userId, AlcoholIntakeRequest req) {
		// 직접 입력 우선순위 확인
		Boolean useCustomInput = req.getUseCustomInput() != null && req.getUseCustomInput();
		double finalAmount;
		Double finalAbv = null;
		
		if (useCustomInput && req.getCustomAbv() != null && req.getCustomVolumeMl() != null) {
			// 직접 입력 모드: customAbv와 customVolumeMl 사용
			double volumePerCup = req.getCustomVolumeMl();
			int cupCount = req.getCupCount() != null ? req.getCupCount() : 1;
			finalAmount = volumePerCup * cupCount;
			finalAbv = req.getCustomAbv();
		} else if (req.getVolumeMl() != null && req.getVolumeMl() > 0.0) {
			// volumeMl이 직접 제공된 경우 그대로 사용
			finalAmount = req.getVolumeMl();
			// volumeMl만 제공되고 customAbv가 있으면 그것 사용, 없으면 카테고리 기본값
			if (req.getCustomAbv() != null && req.getCustomAbv() > 0.0) {
				finalAbv = req.getCustomAbv();
			} else {
				finalAbv = getAbvByType(req.getAlcoholType());
			}
		} else {
			// 카테고리 선택 모드: 기본 용량 또는 커스텀 용량과 잔수로 계산
			double volumePerCup;
			if (req.getCustomVolumeMl() != null && req.getCustomVolumeMl() > 0.0) {
				// 사용자가 잔 크기를 변경한 경우
				volumePerCup = req.getCustomVolumeMl();
			} else {
				// 기본 용량 사용
				volumePerCup = getDefaultVolumeByType(req.getAlcoholType());
			}
			
			// 잔수 계산 (기본값 1잔)
			int cupCount = req.getCupCount() != null ? req.getCupCount() : 1;
			finalAmount = volumePerCup * cupCount;
			finalAbv = getAbvByType(req.getAlcoholType());
		}

		LocalDateTime intakeAt = resolveIntakeAt(
			req.getIntakeAt(),
			req.getMeridiem(),
			req.getHour(),
			req.getMinute()
		);

		Intake intake = Intake.builder()
			.userId(userId)  // JWT 토큰에서 가져온 userId 사용
			.beverageName(req.getAlcoholType())
			.amount(finalAmount)
			.intakeType(IntakeType.ALCOHOL)
			.abv(finalAbv)  // 도수 저장
			.createdAt(req.getIntakeAt() != null ? req.getIntakeAt() : LocalDateTime.now())
			.build();

		intakeRepository.save(intake);

		return new IntakeResponse(
			intake.getIntakeId(),
			intake.getUserId(),
			intake.getBeverageName(),
			intake.getAmount(),
			IntakeType.ALCOHOL.name(),
			intake.getCreatedAt()
		);

	}

	// ===========================
	// 3️⃣ 카페인 잔존 타이머 계산
	// ===========================
	/**
	 * 카페인 섭취 후 약 복용 가능 시간 계산
	 * 
	 * @param userId 사용자 ID
	 * @param intakeId 섭취 기록 ID
	 * @return 잔존 타이머 정보
	 */
	public ResidualTimerResponse calculateCaffeineResidualTimer(Long userId, Long intakeId) {
		// 섭취 기록 조회
		Intake intake = intakeRepository.findById(intakeId)
			.orElseThrow(() -> new IllegalArgumentException("섭취 기록을 찾을 수 없습니다."));
		
		// 사용자 확인 (null-safe 비교)
		Long intakeUserId = intake.getUserId();
		if (!Objects.equals(intakeUserId, userId)) {
			log.warn("사용자 ID 불일치 - 요청 userId: {}, 섭취 기록 userId: {}, intakeId: {}", userId, intakeUserId, intakeId);
			throw new IllegalArgumentException("본인의 섭취 기록만 조회할 수 있습니다.");
		}
		
		// 카페인 타입 확인
		if (intake.getIntakeType() != IntakeType.CAFFEINE) {
			throw new IllegalArgumentException("카페인 섭취 기록이 아닙니다.");
		}
		
		// 사용자 정보 조회 (카페인 민감도)
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		
		CaffeineSensitivity sensitivity = user.getCaffeineSensitivity();
		if (sensitivity == null) {
			sensitivity = CaffeineSensitivity.NORMAL; // 기본값
		}
		
		double halfLifeHours = sensitivity.getHalfLifeHours();
		
		// 현재 시간과 섭취 시각 비교
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime intakeAt = intake.getCreatedAt();
		
		// 경과 시간 계산 (초 단위)
		long secondsPassed = ChronoUnit.SECONDS.between(intakeAt, now);
		
		// 섭취 시각이 미래인 경우 0으로 처리 (아직 섭취하지 않은 경우)
		if (secondsPassed < 0) {
			log.warn("섭취 시각이 미래입니다. intakeAt: {}, now: {}, intakeId: {}", intakeAt, now, intakeId);
			secondsPassed = 0;
		}
		
		double hoursPassed = secondsPassed / 3600.0;
		
		// 초기 섭취량 (mg)
		double initialMg = intake.getAmount();
		
		// 현재 잔존량 계산 (지수감소 모델)
		// 잔존량(mg) = 초기 섭취 mg × (0.5)^(경과시간 / 반감기)
		double currentMg = initialMg * Math.pow(0.5, hoursPassed / halfLifeHours);
		
		// 디버깅 로그
		log.debug("카페인 잔존량 계산 - intakeId: {}, intakeAt: {}, now: {}, hoursPassed: {}h, initialMg: {}mg, halfLifeHours: {}h, currentMg: {}mg",
			intakeId, intakeAt, now, String.format("%.2f", hoursPassed), String.format("%.2f", initialMg), 
			String.format("%.2f", halfLifeHours), String.format("%.2f", currentMg));
		
		// 사용자가 복용 중인 약물 중 가장 높은 보정계수 찾기
		double maxAdjustmentFactor = findMaxAdjustmentFactor(userId);
		
		// 30mg 미만이 되기까지 필요한 시간 계산
		// 30 = initialMg × (0.5)^(t / halfLifeHours)
		// t = halfLifeHours × log2(initialMg / 30)
		double timeToThresholdHours = 0.0;
		if (currentMg > CAFFEINE_THRESHOLD_MG) {
			timeToThresholdHours = halfLifeHours * (Math.log(currentMg / CAFFEINE_THRESHOLD_MG) / Math.log(2.0));
		}
		
		// 약물군 보정계수 적용
		double finalTimeHours = timeToThresholdHours * maxAdjustmentFactor;
		
		// 복약 가능 예상 시각
		LocalDateTime expectedSafeTime = now.plusSeconds((long)(finalTimeHours * 3600));
		
		// 남은 시간 (초)
		long remainingSec = (long)(finalTimeHours * 3600);
		if (remainingSec < 0) {
			remainingSec = 0;
		}
		
		// 이미 복약 가능한지 여부
		boolean isSafe = currentMg <= CAFFEINE_THRESHOLD_MG;
		
		// 가정값들
		Map<String, Object> assumptions = new HashMap<>();
		assumptions.put("halfLifeHours", halfLifeHours);
		assumptions.put("hoursPassed", hoursPassed);
		assumptions.put("initialMg", initialMg);
		assumptions.put("adjustmentFactor", maxAdjustmentFactor);
		
		return ResidualTimerResponse.builder()
			.intakeType("CAFFEINE")
			.currentAmount(currentMg)
			.threshold(CAFFEINE_THRESHOLD_MG)
			.adjustmentFactor(maxAdjustmentFactor)
			.expectedSafeTime(expectedSafeTime)
			.remainingSec(remainingSec)
			.isSafe(isSafe)
			.assumptions(assumptions)
			.build();
	}

	// ===========================
	// 4️⃣ 알코올 잔존 타이머 계산
	// ===========================
	/**
	 * 알코올 섭취 후 약 복용 가능 시간 계산
	 * 
	 * @param userId 사용자 ID
	 * @param intakeId 섭취 기록 ID
	 * @return 잔존 타이머 정보
	 */
	public ResidualTimerResponse calculateAlcoholResidualTimer(Long userId, Long intakeId) {
		// 섭취 기록 조회
		Intake intake = intakeRepository.findById(intakeId)
			.orElseThrow(() -> new IllegalArgumentException("섭취 기록을 찾을 수 없습니다."));
		
		// 사용자 확인 (null-safe 비교)
		Long intakeUserId = intake.getUserId();
		if (!Objects.equals(intakeUserId, userId)) {
			log.warn("사용자 ID 불일치 - 요청 userId: {}, 섭취 기록 userId: {}, intakeId: {}", userId, intakeUserId, intakeId);
			throw new IllegalArgumentException("본인의 섭취 기록만 조회할 수 있습니다.");
		}
		
		// 알코올 타입 확인
		if (intake.getIntakeType() != IntakeType.ALCOHOL) {
			throw new IllegalArgumentException("알코올 섭취 기록이 아닙니다.");
		}
		
		// 사용자 정보 조회 (음주 패턴)
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		
		DrinkingPattern pattern = user.getDrinkingPattern();
		if (pattern == null) {
			pattern = DrinkingPattern.SOMETIMES; // 기본값
		}
		
		double rate = pattern.getRate(); // %BAC per 시간
		
		// 현재 시간과 섭취 시각 비교
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime intakeAt = intake.getCreatedAt();
		double hoursPassed = ChronoUnit.SECONDS.between(intakeAt, now) / 3600.0;
		
		// 술 종류별 기본 용량 및 도수 가져오기
		String alcoholType = intake.getBeverageName();
		double volumeMl = intake.getAmount();
		// 저장된 도수가 있으면 그것 사용, 없으면 카테고리 기본값 사용
		double abv = intake.getAbv() != null ? intake.getAbv() : getAbvByType(alcoholType);
		
		// 표준잔 수 계산
		// 에탄올 g = 용량(mL) × ABV(%) × 0.789
		double ethanolG = volumeMl * (abv / 100.0) * ETHANOL_DENSITY;
		double standardDrinks = ethanolG / STANDARD_DRINK_ALCOHOL_G;
		
		// BAC_peak 계산
		// BAC_peak ≈ 0.02 × (표준잔 수)
		double bacPeak = 0.02 * standardDrinks;
		
		// 현재 BAC 계산
		// BAC_now = max(0, BAC_peak − rate × 경과시간_h)
		double bacNow = Math.max(0.0, bacPeak - (rate * hoursPassed));
		
		// 0.02% 미만이 되기까지 필요한 시간 계산
		// t_to_threshold = max(0, (BAC_now − 0.02) ÷ rate)
		double timeToThresholdHours = 0.0;
		if (bacNow > ALCOHOL_THRESHOLD_BAC) {
			timeToThresholdHours = (bacNow - ALCOHOL_THRESHOLD_BAC) / rate;
		}
		
		// 사용자가 복용 중인 약물 중 가장 높은 보정계수 찾기
		double maxAdjustmentFactor = findMaxAdjustmentFactor(userId);
		
		// 약물군 보정계수 적용
		double finalTimeHours = timeToThresholdHours * maxAdjustmentFactor;
		
		// 복약 가능 예상 시각
		LocalDateTime expectedSafeTime = now.plusSeconds((long)(finalTimeHours * 3600));
		
		// 남은 시간 (초)
		long remainingSec = (long)(finalTimeHours * 3600);
		if (remainingSec < 0) {
			remainingSec = 0;
		}
		
		// 이미 복약 가능한지 여부
		boolean isSafe = bacNow <= ALCOHOL_THRESHOLD_BAC;
		
		// 가정값들
		Map<String, Object> assumptions = new HashMap<>();
		assumptions.put("rate", rate);
		assumptions.put("hoursPassed", hoursPassed);
		assumptions.put("volumeMl", volumeMl);
		assumptions.put("abv", abv);
		assumptions.put("standardDrinks", standardDrinks);
		assumptions.put("bacPeak", bacPeak);
		assumptions.put("adjustmentFactor", maxAdjustmentFactor);
		
		return ResidualTimerResponse.builder()
			.intakeType("ALCOHOL")
			.currentAmount(bacNow * 100) // %BAC를 백분율로 변환
			.threshold(ALCOHOL_THRESHOLD_BAC * 100) // %BAC를 백분율로 변환
			.adjustmentFactor(maxAdjustmentFactor)
			.expectedSafeTime(expectedSafeTime)
			.remainingSec(remainingSec)
			.isSafe(isSafe)
			.assumptions(assumptions)
			.build();
	}

	/**
	 * 사용자의 복약 일정에서 현재 날짜의 약물에 대한 보정계수 찾기
	 * 
	 * 로직:
	 * 1. 현재 날짜의 복약 일정 조회 (SCHEDULED 상태만)
	 * 2. 일정이 없으면 보정계수 적용 안 함 (기본값 1.0 반환)
	 * 3. 여러 개의 약물이 있으면 보정계수가 가장 높은 것을 반환
	 * 
	 * @param userId 사용자 ID
	 * @return 약물군 보정계수 (없으면 1.0, 여러 개면 최대값)
	 */
	private double findMaxAdjustmentFactor(Long userId) {
		// 현재 날짜 기준으로 복약 일정 조회 (SCHEDULED 상태만)
		LocalDate today = LocalDate.now();
		List<Schedule> allSchedules = scheduleRepository.findByUserIdAndDate(userId, today);
		List<Schedule> activeSchedules = allSchedules.stream()
			.filter(s -> s.getStatus() == ScheduleStatus.SCHEDULED)
			.collect(Collectors.toList());
		
		log.debug("보정계수 조회 - userId: {}, today: {}, 전체 일정 수: {}, SCHEDULED 일정 수: {}", 
			userId, today, allSchedules.size(), activeSchedules.size());
		
		// 일정이 없으면 보정계수 적용 안 함 (기본값 1.0 반환)
		if (activeSchedules.isEmpty()) {
			log.debug("SCHEDULED 상태 일정이 없어서 보정계수 1.0 반환");
			return 1.0;
		}
		
		// 각 약물의 보정계수 계산하고 최대값 찾기
		// 여러 개의 약물이 있으면 보정계수가 가장 높은 것을 적용
		double maxFactor = 1.0;
		for (Schedule schedule : activeSchedules) {
			String drugIdString = String.valueOf(schedule.getDrugId());
			double factor = drugClassificationService.getCaffeineAdjustmentFactor(drugIdString);
			log.debug("약물 보정계수 조회 - scheduleId: {}, drugId: {}, drugName: {}, factor: {}", 
				schedule.getScheduleId(), schedule.getDrugId(), schedule.getDrugName(), factor);
			if (factor > maxFactor) {
				maxFactor = factor;
			}
		}
		
		log.debug("최종 보정계수: {}", maxFactor);
		return maxFactor;
	}

	/**
	 * 술 종류별 도수(ABV) 반환
	 */
	private double getAbvByType(String alcoholType) {
		if (alcoholType == null) {
			return BEER_ABV; // 기본값
		}
		
		switch (alcoholType.toLowerCase()) {
			case "맥주":
			case "beer":
				return BEER_ABV;
			case "소주":
			case "soju":
				return SOJU_ABV;
			case "와인":
			case "wine":
				return WINE_ABV;
			case "위스키":
			case "whiskey":
			case "위스키·증류주":
				return WHISKEY_ABV;
			default:
				return BEER_ABV; // 기본값
		}
	}

	/**
	 * 술 종류별 기본 용량(mL) 반환
	 */
	private double getDefaultVolumeByType(String alcoholType) {
		if (alcoholType == null) {
			return BEER_VOLUME_ML; // 기본값
		}
		
		switch (alcoholType.toLowerCase()) {
			case "맥주":
			case "beer":
				return BEER_VOLUME_ML;
			case "소주":
			case "soju":
				return SOJU_VOLUME_ML;
			case "와인":
			case "wine":
				return WINE_VOLUME_ML;
			case "위스키":
			case "whiskey":
			case "위스키·증류주":
				return WHISKEY_VOLUME_ML;
			default:
				return BEER_VOLUME_ML; // 기본값
		}
	}

	// ===========================
	// 5️⃣ 활성 타이머 리스트 조회
	// ===========================
	/**
	 * 사용자의 활성 타이머 리스트 조회 (카페인/알코올 최신 1개씩)
	 * 
	 * @param userId 사용자 ID
	 * @return 활성 타이머 리스트
	 */
	public ActiveTimerListResponse getActiveTimers(Long userId) {
		ActiveTimerListResponse.ActiveTimerItem caffeineTimer = null;
		ActiveTimerListResponse.ActiveTimerItem alcoholTimer = null;
		
		// 카페인 최신 기록 조회
		Optional<Intake> latestCaffeine = intakeRepository.findTopByUserIdAndIntakeTypeOrderByCreatedAtDesc(userId, IntakeType.CAFFEINE);
		if (latestCaffeine.isPresent()) {
			Intake intake = latestCaffeine.get();
			ResidualTimerResponse timer = calculateCaffeineResidualTimer(userId, intake.getIntakeId());
			
			// 활성 타이머만 포함 (아직 복약 불가능한 경우)
			if (timer != null && !timer.getIsSafe()) {
				caffeineTimer = ActiveTimerListResponse.ActiveTimerItem.builder()
					.intakeId(intake.getIntakeId())
					.intakeType("CAFFEINE")
					.name(intake.getBeverageName())
					.amount(intake.getAmount())
					.abv(null)  // 카페인은 도수 없음
					.intakeAt(intake.getCreatedAt())
					.currentAmount(timer.getCurrentAmount())
					.remainingSec(timer.getRemainingSec())
					.expectedSafeTime(timer.getExpectedSafeTime())
					.isSafe(timer.getIsSafe())
					.build();
			}
		}
		
		// 알코올 최신 기록 조회
		Optional<Intake> latestAlcohol = intakeRepository.findTopByUserIdAndIntakeTypeOrderByCreatedAtDesc(userId, IntakeType.ALCOHOL);
		if (latestAlcohol.isPresent()) {
			Intake intake = latestAlcohol.get();
			ResidualTimerResponse timer = calculateAlcoholResidualTimer(userId, intake.getIntakeId());
			
			// 활성 타이머만 포함 (아직 복약 불가능한 경우)
			if (timer != null && !timer.getIsSafe()) {
				alcoholTimer = ActiveTimerListResponse.ActiveTimerItem.builder()
					.intakeId(intake.getIntakeId())
					.intakeType("ALCOHOL")
					.name(intake.getBeverageName())
					.amount(intake.getAmount())
					.abv(intake.getAbv() != null ? intake.getAbv() : getAbvByType(intake.getBeverageName()))
					.intakeAt(intake.getCreatedAt())
					.currentAmount(timer.getCurrentAmount())
					.remainingSec(timer.getRemainingSec())
					.expectedSafeTime(timer.getExpectedSafeTime())
					.isSafe(timer.getIsSafe())
					.build();
			}
		}
		
		return ActiveTimerListResponse.builder()
			.caffeineTimer(caffeineTimer)
			.alcoholTimer(alcoholTimer)
			.build();
	}
	private LocalDateTime resolveIntakeAt(LocalDateTime base,
		String meridiem,
		Integer hour,
		Integer minute) {

		// 세 개 중 하나라도 없으면 예전 방식 유지
		if (meridiem == null || hour == null || minute == null) {
			return (base != null) ? base : LocalDateTime.now();
		}

		// 12시간제 → 24시간제 변환
		int h = hour % 12; // 12시는 0으로
		if ("오후".equals(meridiem) || "PM".equalsIgnoreCase(meridiem)) {
			h += 12;
		}

		// 날짜: base가 있으면 그 날짜, 없으면 오늘
		LocalDate date = (base != null) ? base.toLocalDate() : LocalDate.now();

		return LocalDateTime.of(date, LocalTime.of(h, minute));
	}

}
