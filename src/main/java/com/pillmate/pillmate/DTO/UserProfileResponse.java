package com.pillmate.pillmate.DTO;

import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Domain.CaffeineSensitivity;
import com.pillmate.pillmate.Domain.DrinkingPattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {

	@Schema(description = "사용자 ID", example = "1")
	private Long userId;

	@Schema(description = "이름", example = "홍길동")
	private String name;

	@Schema(description = "이메일", example = "test@gmail.com")
	private String email;

	@Schema(description = "프로필 이미지 URL", example = "https://example.com/image.png")
	private String profileImage;

	@Schema(description = "카페인 민감도", example = "NORMAL", allowableValues = {"WEAK","NORMAL","STRONG"})
	private CaffeineSensitivity caffeineSensitivity;

	@Schema(description = "음주 패턴", example = "SOMETIMES", allowableValues = {"NONE","SOMETIMES","OFTEN"})
	private DrinkingPattern alcoholPattern;

	public static UserProfileResponse from(User user) {
		return UserProfileResponse.builder()
			.userId(user.getId())
			.name(user.getName())
			.email(user.getEmail())
			.profileImage(user.getProfileImage())
			.caffeineSensitivity(user.getCaffeineSensitivity())
			.alcoholPattern(user.getDrinkingPattern())
			.build();
	}
}
