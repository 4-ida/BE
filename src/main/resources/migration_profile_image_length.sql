-- profile_image 컬럼 길이 확장 마이그레이션
-- VARCHAR(255)에서 VARCHAR(10000)으로 변경하여 긴 URL(base64 인코딩된 이미지 등)도 저장 가능하도록 함

ALTER TABLE users 
MODIFY COLUMN profile_image VARCHAR(10000) COMMENT '프로필 이미지 URL';

