package com.pillmate.pillmate.DTO;

public record DashboardProgressResponse(
	Long userId,
	int totalPlannedIntakes,   // 기간 동안 해야 하는 횟수
	int completedIntakes,      // 실제로 한 횟수 (캘린더 체크수)
	int missedIntakes,         // 누락 = 계획 - 실제
	double adherenceRate       // 진행률 (%)
) {}
