-- 타이레놀 제품 목 데이터 (식약처 API에 없는 정보 보완)

-- 1. 타이레놀콜드-에스정 (감기약)
INSERT INTO drug_manual_overrides (drug_id, strength, ingredients_csv, caution_alcohol, caution_caffeine)
VALUES (
    '202106954',
    '아세트아미노펜 300mg, 클로르페니라민말레산염 2.5mg, 덱스트로메토르판브롬화수소산염수화물 15mg, 슈도에페드린염산염 30mg',
    '아세트아미노펜,클로르페니라민말레산염,덱스트로메토르판브롬화수소산염수화물,슈도에페드린염산염',
    '복용 중 및 복용 전후 음주를 피하십시오. 간 손상 위험이 증가할 수 있습니다.',
    '카페인과 함께 복용 시 각성 효과가 증가할 수 있습니다.'
)
ON DUPLICATE KEY UPDATE
    strength = VALUES(strength),
    ingredients_csv = VALUES(ingredients_csv),
    caution_alcohol = VALUES(caution_alcohol),
    caution_caffeine = VALUES(caution_caffeine);

-- 2. 타이레놀정500밀리그람(아세트아미노펜)
INSERT INTO drug_manual_overrides (drug_id, strength, ingredients_csv, caution_alcohol, caution_caffeine)
VALUES (
    '202106092',
    '아세트아미노펜 500mg',
    '아세트아미노펜',
    '복용 중 및 복용 전후 음주를 피하십시오. 간 손상 위험이 증가할 수 있습니다.',
    '일반적으로 카페인과의 상호작용은 크지 않으나, 과량 복용 시 주의하세요.'
)
ON DUPLICATE KEY UPDATE
    strength = VALUES(strength),
    ingredients_csv = VALUES(ingredients_csv),
    caution_alcohol = VALUES(caution_alcohol),
    caution_caffeine = VALUES(caution_caffeine);

-- 3. 타이레놀8시간이알서방정(아세트아미노펜)
INSERT INTO drug_manual_overrides (drug_id, strength, ingredients_csv, caution_alcohol, caution_caffeine)
VALUES (
    '202200407',
    '아세트아미노펜 650mg (서방정)',
    '아세트아미노펜',
    '복용 중 및 복용 전후 음주를 피하십시오. 간 손상 위험이 증가할 수 있습니다.',
    '일반적으로 카페인과의 상호작용은 크지 않으나, 과량 복용 시 주의하세요.'
)
ON DUPLICATE KEY UPDATE
    strength = VALUES(strength),
    ingredients_csv = VALUES(ingredients_csv),
    caution_alcohol = VALUES(caution_alcohol),
    caution_caffeine = VALUES(caution_caffeine);