package com.pillmate.pillmate.Domain;

/**
 * 음주 패턴
 * 대사속도(rate) 기준으로 분류됩니다.
 * rate는 %BAC per 시간 단위입니다.
 */
public enum DrinkingPattern {
	NONE(0.010),      // 없음 - rate 0.010 (%BAC per 시간)
	SOMETIMES(0.013), // 가끔 - rate 0.013 (%BAC per 시간)
	OFTEN(0.015);     // 자주 - rate 0.015 (%BAC per 시간)

	private final double rate;

	DrinkingPattern(double rate) {
		this.rate = rate;
	}

	/**
	 * 알코올 대사속도(rate)를 반환합니다 (%BAC per 시간)
	 * @return 대사속도 (예: 0.010, 0.013, 0.015)
	 */
	public double getRate() {
		return rate;
	}
}
