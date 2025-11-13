// src/main/java/com/pillmate/pillmate/DTO/CaffeineIntakeRequest.java
package com.pillmate.pillmate.DTO;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaffeineIntakeRequest {
	private String beverageName;     // 커피, 에너지드링크 ...
	private Double caffeineMg;       // 입력한 mg
	private Double intakeRatio;      // 0~100 (%)
	private LocalDateTime intakeAt;  // 섭취 시각
	private String meridiem;         // 예: "오전", "오후"
	private Integer hour;            // 1 ~ 12
	private Integer minute;          // 0 ~ 59
}
