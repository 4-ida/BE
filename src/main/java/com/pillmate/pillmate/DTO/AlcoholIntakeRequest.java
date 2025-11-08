// src/main/java/com/pillmate/pillmate/DTO/AlcoholIntakeRequest.java
package com.pillmate.pillmate.DTO;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlcoholIntakeRequest {
	private Long userId;
	private String alcoholType;      // 맥주, 소주, 와인, 위스키
	private Double volumeMl;         // 최종 ml (기본용량 or 변경용량 * 잔수로 계산해서 프론트가 보내도 됨)
	private Integer cupCount;        // 몇 잔 마셨는지 (선택)
	private LocalDateTime intakeAt;  // 섭취 시각
}
