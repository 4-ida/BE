package com.pillmate.pillmate.DTO;

import com.pillmate.pillmate.Domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileResponse {

	private Long userId;
	private String name;
	private String email;
	private String profileImage;
	private String caffeineSensitivity;
	private String drinkingPattern;

	public static UserProfileResponse from(User user) {
		UserProfileResponse dto = new UserProfileResponse();
		dto.userId = user.getId();
		dto.name = user.getName();
		dto.email = user.getEmail();
		dto.profileImage = user.getProfileImage();
		dto.caffeineSensitivity = user.getCaffeineSensitivity();
		dto.drinkingPattern = user.getDrinkingPattern();
		return dto;
	}
}
