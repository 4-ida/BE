package com.pillmate.pillmate.Domain;

/**
 * 카페인 민감도
 * 반감기 기준으로 분류됩니다.
 */
public enum CaffeineSensitivity {
	WEAK(4.0),      // 약함 - 반감기 4시간
	NORMAL(5.0),   // 보통 - 반감기 5시간
	STRONG(6.5);   // 강함 - 반감기 6.5시간

	private final double halfLifeHours;

	CaffeineSensitivity(double halfLifeHours) {
		this.halfLifeHours = halfLifeHours;
	}

	/**
	 * 카페인 반감기 시간을 반환합니다 (시간 단위)
	 * @return 반감기 시간 (예: 4.0, 5.0, 6.5)
	 */
	public double getHalfLifeHours() {
		return halfLifeHours;
	}
}
