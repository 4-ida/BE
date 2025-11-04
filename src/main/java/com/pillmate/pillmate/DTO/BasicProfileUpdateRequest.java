package com.pillmate.pillmate.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BasicProfileUpdateRequest {

	private Double defaultCaffeineAmount;  // 수정할 카페인 기본값
	private Double defaultAlcoholAmount;   // 수정할 알코올 기본값
	private String currentMedications;     // 수정할 복용 약 정보
	private String preferredBeverageType;  // 수정할 선호 음료 종류
}
