package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.DTO.DashboardProgressResponse;
import com.pillmate.pillmate.Repository.IntakeRepository;
import com.pillmate.pillmate.Repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final ScheduleRepository scheduleRepository;
	private final IntakeRepository intakeRepository;

	public DashboardProgressResponse getProgress(Long userId) {

		// 1. 이 사용자에게 등록된 전체 복용 계획 개수
		int totalPlanned = scheduleRepository.countByUserId(userId);

		// 2. 실제 섭취(기록)된 개수
		int completed = intakeRepository.countByUserId(userId);

		// 3. 누락
		int missed = Math.max(totalPlanned - completed, 0);

		// 4. 진행률
		double adherence = (totalPlanned == 0)
			? 0.0
			: (completed * 100.0) / totalPlanned;

		return new DashboardProgressResponse(
			userId,
			totalPlanned,
			completed,
			missed,
			adherence
		);
	}
}
