package com.pillmate.pillmate.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardProgressResponse {

	private Long userId;          // 사용자 PK
	private String userName;      // "홍길동"
	private String periodLabel;   // "2025년 11월"

	private int totalPlanned;     // 등록된 일정 개수
	private int completed;        // 실제 섭취(기록) 개수
	private int missed;           // 누락 개수

	private int progressPercent;  // 0~100
	private String message;       // "홍길동님의 11월 복용률 : 73%"
}
