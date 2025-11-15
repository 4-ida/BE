package com.pillmate.pillmate.Config;

import com.pillmate.pillmate.Domain.DrugManualOverride;
import com.pillmate.pillmate.Repository.DrugManualOverrideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TylenolDataLoader implements CommandLineRunner {

    private final DrugManualOverrideRepository drugManualOverrideRepository;

    @Override
    public void run(String... args) {
        loadTylenolData();
    }

    private void loadTylenolData() {
        // 1. 타이레놀콜드-에스정 (감기약)
        saveDrugOverride(
            "202106954",
            "아세트아미노펜 300mg, 클로르페니라민말레산염 2.5mg, 덱스트로메토르판브롬화수소산염수화물 15mg, 슈도에페드린염산염 30mg",
            "아세트아미노펜,클로르페니라민말레산염,덱스트로메토르판브롬화수소산염수화물,슈도에페드린염산염",
            "복용 중 및 복용 전후 음주를 피하십시오. 간 손상 위험이 증가할 수 있습니다.",
            "카페인과 함께 복용 시 각성 효과가 증가할 수 있습니다."
        );

        // 2. 타이레놀정500밀리그람(아세트아미노펜)
        saveDrugOverride(
            "202106092",
            "아세트아미노펜 500mg",
            "아세트아미노펜",
            "복용 중 및 복용 전후 음주를 피하십시오. 간 손상 위험이 증가할 수 있습니다.",
            "일반적으로 카페인과의 상호작용은 크지 않으나, 과량 복용 시 주의하세요."
        );

        // 3. 타이레놀8시간이알서방정(아세트아미노펜)
        saveDrugOverride(
            "202200407",
            "아세트아미노펜 650mg (서방정)",
            "아세트아미노펜",
            "복용 중 및 복용 전후 음주를 피하십시오. 간 손상 위험이 증가할 수 있습니다.",
            "일반적으로 카페인과의 상호작용은 크지 않으나, 과량 복용 시 주의하세요."
        );

        log.info("✅ 타이레놀 제품 목 데이터 로드 완료");
    }

    private void saveDrugOverride(String drugId, String strength, String ingredientsCsv,
                                   String cautionAlcohol, String cautionCaffeine) {
        if (drugManualOverrideRepository.existsById(drugId)) {
            log.debug("이미 존재하는 데이터: drugId={}", drugId);
            return;
        }

        DrugManualOverride override = DrugManualOverride.builder()
                .drugId(drugId)
                .strength(strength)
                .ingredientsCsv(ingredientsCsv)
                .cautionAlcohol(cautionAlcohol)
                .cautionCaffeine(cautionCaffeine)
                .build();

        drugManualOverrideRepository.save(override);
        log.info("타이레놀 데이터 저장: drugId={}, name={}", drugId,
            drugId.equals("202106954") ? "타이레놀콜드-에스정" :
            drugId.equals("202106092") ? "타이레놀정500밀리그람" :
            "타이레놀8시간이알서방정");
    }
}