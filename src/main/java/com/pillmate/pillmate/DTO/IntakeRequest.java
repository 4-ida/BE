package com.pillmate.pillmate.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntakeRequest {
	private Long userId;
	private String beverageName;
	private Double amount;
	private String intakeType;  // 여기서는 문자열로 받고 서비스에서 Enum으로 바꿀게
}
