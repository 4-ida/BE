package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.DTO.DashboardProgressResponse;
import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Repository.IntakeRepository;
import com.pillmate.pillmate.Repository.ScheduleRepository;
import com.pillmate.pillmate.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final ScheduleRepository scheduleRepository;
	private final IntakeRepository intakeRepository;
	private final UserRepository userRepository;

	/**
	 * 기본: 이번 달 기준으로 대시보드
	 */
	public DashboardProgressResponse getProgress(Long userId) {
		LocalDate now = LocalDate.now();
		return getProgress(userId, now.getYear(), now.getMonthValue());
	}

	/**
	 * 특정 연/월 기준 대시보드 (쿼리파라미터로 year, month 들어오는 경우)
	 */
	public DashboardProgressResponse getProgress(Long userId, int year, int month) {

		// 0. 사용자 이름 가져오기
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		String userName = user.getName();
		String periodLabel = year + "년 " + month + "월";

		// 1. 이 사용자에게 등록된 전체 복용 계획 개수
		int totalPlanned = scheduleRepository.countByUserId(userId);

		// 2. 실제 섭취(기록)된 개수
		int completed = intakeRepository.countByUserId(userId);

		// 3. 누락
		int missed = Math.max(totalPlanned - completed, 0);

		// 4. 진행률
		int adherencePercent = (totalPlanned == 0)
			? 0
			: (int) Math.round((completed * 100.0) / totalPlanned);

		// 5. 사람이 보는 문장
		String message = userName + "님의 " + periodLabel + " 복용률 : " + adherencePercent + "%";

		return DashboardProgressResponse.builder()
			.userId(userId)
			.userName(userName)
			.periodLabel(periodLabel)
			.totalPlanned(totalPlanned)
			.completed(completed)
			.missed(missed)
			.progressPercent(adherencePercent)
			.message(message)
			.build();
	}
}

