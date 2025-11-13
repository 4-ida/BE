// src/main/java/com/pillmate/pillmate/DTO/AlcoholIntakeRequest.java
package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "알코올 섭취 입력 요청")
public class AlcoholIntakeRequest {
	
	@Schema(description = "술 종류 (카테고리 선택 시)", example = "맥주", allowableValues = {"맥주", "소주", "와인", "위스키"})
	private String alcoholType;
	
	@Schema(description = "잔 수 (기본값: 1)", example = "2", minimum = "1")
	private Integer cupCount;
	
	@Schema(description = "잔 크기 변경 시 개별 잔 용량(mL). 카테고리 선택 시 기본 용량 대신 사용", example = "330.0")
	private Double customVolumeMl;
	
	@Schema(description = "사용자가 직접 입력한 총 용량(mL). volumeMl이 제공되면 잔 단위 계산 없이 그대로 사용", example = "1000.0")
	private Double volumeMl;
	
	@Schema(description = "직접 입력 사용 여부. true면 customAbv와 customVolumeMl을 사용 (우선순위 최상)", example = "false")
	private Boolean useCustomInput;
	
	@Schema(description = "직접 입력한 도수(%). useCustomInput이 true일 때 필수", example = "5.0", minimum = "0", maximum = "100")
	private Double customAbv;
	
	@Schema(description = "섭취 시각 (null이면 현재 시간)", example = "2025-11-12T18:00:00")
	private LocalDateTime intakeAt;

	@Schema(description = "오전/오후 정보", example = "오후")
	private String meridiem;   // "오전"/"오후" 또는 "AM"/"PM"

	@Schema(description = "시 (1~12시 형태)", example = "6")
	private Integer hour;

	@Schema(description = "분 (0~59)", example = "0")
	private Integer minute;
}
