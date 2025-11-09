-- 회원가입 API를 위한 users 테이블 마이그레이션 (MySQL)
-- 기존 테이블이 있는 경우에 실행할 SQL 스크립트

-- 먼저 컬럼이 존재하는지 확인 후 실행하세요
-- 또는 아래 쿼리를 하나씩 실행하면서 에러가 나면 해당 컬럼은 이미 존재하는 것입니다

-- 1. 약관 동의 필드 추가
ALTER TABLE users 
ADD COLUMN terms_of_service BOOLEAN NOT NULL DEFAULT FALSE COMMENT '서비스 이용약관 동의 여부';

ALTER TABLE users 
ADD COLUMN privacy_policy BOOLEAN NOT NULL DEFAULT FALSE COMMENT '개인정보 처리방침 동의 여부';

ALTER TABLE users 
ADD COLUMN data_usage BOOLEAN NOT NULL DEFAULT FALSE COMMENT '데이터 활용 동의 여부';

-- 2. 생성일시, 수정일시 필드 추가
ALTER TABLE users 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '회원 생성 시각';

ALTER TABLE users 
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '정보 최종 수정 시각';

-- 3. name 컬럼 길이 변경 (50 -> 20)
ALTER TABLE users 
MODIFY COLUMN name VARCHAR(20) NOT NULL COMMENT '사용자 이름';

-- 4. schedules 테이블 보강
ALTER TABLE schedules 
ADD COLUMN user_id BIGINT NOT NULL COMMENT '사용자 ID';

ALTER TABLE schedules 
ADD COLUMN drug_name VARCHAR(150) COMMENT '약품명';


