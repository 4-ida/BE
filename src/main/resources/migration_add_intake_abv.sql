-- intakes 테이블에 알코올 도수(ABV) 컬럼 추가
-- 직접 입력한 도수를 저장하기 위한 컬럼

ALTER TABLE intakes 
ADD COLUMN abv DOUBLE NULL COMMENT '알코올 도수(%) - ALCOHOL 타입일 때만 사용, 직접 입력 시 저장';

-- 기존 데이터는 NULL로 유지 (카테고리 기본값 사용)


