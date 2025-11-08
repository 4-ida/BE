-- users 테이블 생성 스크립트 (새로 테이블을 만드는 경우)

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL COMMENT '사용자 이름',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호 (암호화)',
    terms_of_service BOOLEAN NOT NULL DEFAULT FALSE COMMENT '서비스 이용약관 동의 여부',
    privacy_policy BOOLEAN NOT NULL DEFAULT FALSE COMMENT '개인정보 처리방침 동의 여부',
    data_usage BOOLEAN NOT NULL DEFAULT FALSE COMMENT '데이터 활용 동의 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '회원 생성 시각',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '정보 최종 수정 시각',
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보 테이블';

-- schedules 테이블 생성 스크립트 (캘린더 복약 일정)

CREATE TABLE IF NOT EXISTS schedules (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_id BIGINT NOT NULL COMMENT '약품 ID',
    dose VARCHAR(100) NOT NULL COMMENT '복용량 또는 용법',
    alarm_at DATETIME NOT NULL COMMENT '알림 시각',
    memo TEXT COMMENT '사용자 메모',
    alarm_enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '알림 사용 여부',
    repeat_rule VARCHAR(500) COMMENT '반복 규칙 (RFC5545 형식)',
    start_date DATE NOT NULL COMMENT '복용 시작일',
    end_date DATE NOT NULL COMMENT '복용 종료일',
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT '일정 상태 (SCHEDULED, COMPLETED, MISSED, CANCELLED)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '일정 생성 시각',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '일정 수정 시각',
    INDEX idx_drug_id (drug_id),
    INDEX idx_alarm_at (alarm_at),
    INDEX idx_date_range (start_date, end_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='캘린더 복약 일정 테이블';

-- 약품 수동 보강 데이터를 저장하는 테이블
CREATE TABLE IF NOT EXISTS drug_manual_overrides (
    drug_id VARCHAR(32) NOT NULL COMMENT '식약처 품목기준코드',
    strength VARCHAR(255) COMMENT '수동으로 정의한 함량 정보',
    ingredients_csv VARCHAR(1000) COMMENT '쉼표로 구분된 주요 성분 목록',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 시각',
    PRIMARY KEY (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='식약처 API 보완용 약품 정보';

