package com.pillmate.pillmate.DTO;

import com.pillmate.pillmate.Domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BasicProfileResponse {

	private Long userId;                   // 사용자 ID
	private Double defaultCaffeineAmount;  // 기본 카페인 함량
	private Double defaultAlcoholAmount;   // 기본 알코올 함량
	private String currentMedications;     // 현재 복용 중인 약
	private String preferredBeverageType;  // 선호 음료 종류 (소주, 맥주, 커피 등)

	public static BasicProfileResponse from(User user) {
		BasicProfileResponse dto = new BasicProfileResponse();
		dto.userId = user.getId();
		dto.defaultCaffeineAmount = user.getDefaultCaffeineAmount();
		dto.defaultAlcoholAmount = user.getDefaultAlcoholAmount();
		dto.currentMedications = user.getCurrentMedications();
		dto.preferredBeverageType = user.getPreferredBeverageType();
		return dto;
	}
}
