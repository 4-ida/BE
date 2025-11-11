package com.pillmate.pillmate.DTO;

import com.pillmate.pillmate.Domain.CaffeineSensitivity;
import com.pillmate.pillmate.Domain.DrinkingPattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {

	private String name;

	@Schema(description = "카페인 민감도", example = "NORMAL", allowableValues = {"WEAK","NORMAL","STRONG"})
	private CaffeineSensitivity caffeineSensitivity;

	@Schema(description = "음주 패턴", example = "SOMETIMES", allowableValues = {"NONE","SOMETIMES","OFTEN"})
	private DrinkingPattern alcoholPattern;
}
