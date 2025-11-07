package com.pillmate.pillmate.Repository;

import com.pillmate.pillmate.Domain.Intake;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IntakeRepository extends JpaRepository<Intake, Long> {

	// 사용자별 전체 섭취 기록
	List<Intake> findByUserId(Long userId);

	// 사용자 기록 전체 삭제
	void deleteByUserId(Long userId);

	// 대시보드 진행률용
	int countByUserId(Long userId);

	// 약물(섭취) 알림용: 특정 시각 이후의 기록만
	List<Intake> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);
}
