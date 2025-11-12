package com.pillmate.pillmate.Domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "intakes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intake {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long intakeId;          // 응답에서 쓰는 이름에 맞춰줌

	private Long userId;            // body에서 받는 거 그대로

	private String beverageName;    // "아메리카노", "맥주" 같은 이름

	private Double amount;          // 355.5

	@Enumerated(EnumType.STRING)
	private IntakeType intakeType;  // "CAFFEINE" 이런 거

	private Double abv;             // 알코올 도수(%) - ALCOHOL 타입일 때만 사용, 직접 입력 시 저장

	private LocalDateTime createdAt;
	public IntakeType getIntakeType() {
		return this.intakeType;
	}
}
