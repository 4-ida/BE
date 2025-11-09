-- 회원가입 API를 위한 users 테이블 마이그레이션
-- 기존 테이블이 있는 경우에 실행할 SQL 스크립트

-- 1. 약관 동의 필드 추가 (없는 경우에만)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS terms_of_service BOOLEAN NOT NULL DEFAULT FALSE COMMENT '서비스 이용약관 동의 여부';

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS privacy_policy BOOLEAN NOT NULL DEFAULT FALSE COMMENT '개인정보 처리방침 동의 여부';

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS data_usage BOOLEAN NOT NULL DEFAULT FALSE COMMENT '데이터 활용 동의 여부';

-- 2. 생성일시, 수정일시 필드 추가 (없는 경우에만)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '회원 생성 시각';

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '정보 최종 수정 시각';

-- 3. name 컬럼 길이 변경 (50 -> 20)
ALTER TABLE users 
MODIFY COLUMN name VARCHAR(20) NOT NULL COMMENT '사용자 이름';

-- 참고: MySQL 8.0 이상에서 IF NOT EXISTS를 사용할 수 없으므로, 
-- 아래와 같이 수동으로 확인 후 실행해야 할 수 있습니다.


-- 4. 수동 약품 보강 테이블 생성 (없으면 생성)
CREATE TABLE IF NOT EXISTS drug_manual_overrides (
    drug_id VARCHAR(32) NOT NULL COMMENT '식약처 품목기준코드',
    strength VARCHAR(255) COMMENT '수동으로 정의한 함량 정보',
    ingredients_csv VARCHAR(1000) COMMENT '쉼표로 구분된 주요 성분 목록',
    caution_alcohol VARCHAR(1000) COMMENT '음주 관련 주의사항 수동 요약',
    caution_caffeine VARCHAR(1000) COMMENT '카페인 관련 주의사항 수동 요약',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 시각',
    PRIMARY KEY (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='식약처 API 보완용 약품 정보';

