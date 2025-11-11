package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.DTO.DashboardProgressResponse;
import com.pillmate.pillmate.Domain.ScheduleStatus;
import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Repository.ScheduleRepository;
import com.pillmate.pillmate.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final ScheduleRepository scheduleRepository;
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
		// 월 값 검증
		if (month < 1 || month > 12) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "월은 1부터 12 사이의 값이어야 합니다");
		}

		// 사용자 이름 가져오기
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"));

		String userName = user.getName();
		String periodLabel = year + "년 " + month + "월";

		// 해당 월의 첫 날과 마지막 날 계산
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

		// 1. 해당 기간 내에 등록된 전체 복용 계획 개수 (CANCELLED 제외)
		// SCHEDULED, TAKEN, MISSED 상태만 포함
		int totalPlanned = scheduleRepository.countByUserIdAndStatusNotAndDateRange(
			userId, ScheduleStatus.CANCELLED, firstDayOfMonth, lastDayOfMonth);

		// 2. 실제 섭취(기록)된 개수 - TAKEN 상태인 일정 개수
		int completed = scheduleRepository.countByUserIdAndStatusAndDateRange(
			userId, ScheduleStatus.TAKEN, firstDayOfMonth, lastDayOfMonth);

		// 3. 누락 (MISSED 상태인 일정 개수)
		int missed = scheduleRepository.countByUserIdAndStatusAndDateRange(
			userId, ScheduleStatus.MISSED, firstDayOfMonth, lastDayOfMonth);

		// 4. 진행률 계산 (totalPlanned가 0이면 0%, 아니면 completed / totalPlanned * 100)
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

