package com.pillmate.pillmate.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {
	private String name;
	private String profileImage;
	private String caffeineSensitivity;
	private String drinkingPattern;
}