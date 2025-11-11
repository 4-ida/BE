package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Service.dto.MfdsPermissionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;

/**
 * 약물군 분류 서비스
 * ATC 코드와 약물명/성분명을 기반으로 약물군을 분류합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugClassificationService {

    private final MfdsDrugPermissionClient mfdsDrugPermissionClient;

    /**
     * 약물군 타입 enum
     */
    public enum DrugCategory {
        ANTIBIOTIC("항생제", 2.0),                    // 항생제(시프로플록사신 등)
        SLEEP_AID("수면제/진정제/항불안제", 1.5),    // 수면제/진정제/항불안제
        ANTIDEPRESSANT("항우울제/MAOI", 1.8),         // 항우울제/MAOI
        ANTIHYPERTENSIVE("항고혈압제", 1.3),          // 항고혈압제
        GENERAL("일반 진통제/비타민/영양제", 1.0),    // 일반 진통제/비타민/영양제
        CONTRACEPTIVE("피임약/호르몬제", 1.5);        // 피임약/호르몬제

        private final String name;
        private final double caffeineAdjustmentFactor;

        DrugCategory(String name, double caffeineAdjustmentFactor) {
            this.name = name;
            this.caffeineAdjustmentFactor = caffeineAdjustmentFactor;
        }

        public String getName() {
            return name;
        }

        public double getCaffeineAdjustmentFactor() {
            return caffeineAdjustmentFactor;
        }
    }

    /**
     * 약품 ID(itemSeq)로부터 카페인 보정 계수를 조회합니다.
     * 
     * @param itemSeq 식약처 품목기준코드
     * @return 카페인 보정 계수 (기본값 1.0)
     */
    public double getCaffeineAdjustmentFactor(String itemSeq) {
        if (!StringUtils.hasText(itemSeq)) {
            log.warn("itemSeq is empty, returning default adjustment factor");
            return DrugCategory.GENERAL.getCaffeineAdjustmentFactor();
        }

        DrugCategory category = classifyDrug(itemSeq);
        return category.getCaffeineAdjustmentFactor();
    }

    /**
     * 약물군을 분류합니다.
     * 
     * @param itemSeq 식약처 품목기준코드
     * @return 약물군 카테고리
     */
    public DrugCategory classifyDrug(String itemSeq) {
        if (!StringUtils.hasText(itemSeq)) {
            return DrugCategory.GENERAL;
        }

        // 1. 식약처 API에서 ATC 코드와 약물 정보 조회
        Optional<MfdsPermissionResponse.PermissionItem> permissionOpt = 
                mfdsDrugPermissionClient.fetchPermission(itemSeq);

        if (permissionOpt.isEmpty()) {
            log.warn("Failed to fetch permission data for itemSeq={}, using default category", itemSeq);
            return DrugCategory.GENERAL;
        }

        MfdsPermissionResponse.PermissionItem permission = permissionOpt.get();
        String atcCode = permission.getResolvedAtcCode();
        String itemName = permission.getResolvedItemName();
        String mainIngredient = permission.getResolvedMainIngredient();

        // 2. ATC 코드 기반 분류
        if (StringUtils.hasText(atcCode)) {
            DrugCategory categoryByAtc = classifyByAtcCode(atcCode);
            if (categoryByAtc != DrugCategory.GENERAL) {
                log.debug("Classified drug {} as {} by ATC code: {}", itemSeq, categoryByAtc.getName(), atcCode);
                return categoryByAtc;
            }
        }

        // 3. 약물명/성분명 키워드 기반 분류
        DrugCategory categoryByName = classifyByNameAndIngredient(itemName, mainIngredient);
        if (categoryByName != DrugCategory.GENERAL) {
            log.debug("Classified drug {} as {} by name/ingredient", itemSeq, categoryByName.getName());
            return categoryByName;
        }

        // 4. 기본값 반환
        return DrugCategory.GENERAL;
    }

    /**
     * ATC 코드를 기반으로 약물군을 분류합니다.
     * 
     * @param atcCode ATC 코드 (예: J01AA02)
     * @return 약물군 카테고리
     */
    private DrugCategory classifyByAtcCode(String atcCode) {
        if (!StringUtils.hasText(atcCode)) {
            return DrugCategory.GENERAL;
        }

        String atcUpper = atcCode.toUpperCase().trim();

        // 항생제: J01 (항균제), J02 (항진균제), J04 (항결핵제)
        if (atcUpper.startsWith("J01") || atcUpper.startsWith("J02") || atcUpper.startsWith("J04")) {
            return DrugCategory.ANTIBIOTIC;
        }

        // 수면제/진정제/항불안제: N05 (정신안정제)
        if (atcUpper.startsWith("N05")) {
            return DrugCategory.SLEEP_AID;
        }

        // 항우울제/MAOI: N06A (항우울제), N06B (정신자극제), N06C (정신안정제)
        if (atcUpper.startsWith("N06A") || atcUpper.startsWith("N06B") || atcUpper.startsWith("N06C")) {
            return DrugCategory.ANTIDEPRESSANT;
        }

        // 항고혈압제: C02 (항고혈압제), C03 (이뇨제), C07 (베타 차단제), C08 (칼슘 채널 차단제), C09 (ACE 억제제/ARB)
        if (atcUpper.startsWith("C02") || atcUpper.startsWith("C03") || 
            atcUpper.startsWith("C07") || atcUpper.startsWith("C08") || 
            atcUpper.startsWith("C09")) {
            return DrugCategory.ANTIHYPERTENSIVE;
        }

        // 일반 진통제: N02 (진통제), M01A (항류마티스제 중 일부)
        if (atcUpper.startsWith("N02")) {
            return DrugCategory.GENERAL;
        }

        // 비타민/영양제: A11 (비타민), A16 (기타 영양제)
        if (atcUpper.startsWith("A11") || atcUpper.startsWith("A16")) {
            return DrugCategory.GENERAL;
        }

        // 피임약/호르몬제: G03 (성 호르몬), G03A (에스트로겐), G03B (프로게스토겐)
        if (atcUpper.startsWith("G03")) {
            return DrugCategory.CONTRACEPTIVE;
        }

        return DrugCategory.GENERAL;
    }

    /**
     * 약물명과 성분명을 기반으로 약물군을 분류합니다.
     * 
     * @param itemName 약물명
     * @param mainIngredient 주성분명
     * @return 약물군 카테고리
     */
    private DrugCategory classifyByNameAndIngredient(String itemName, String mainIngredient) {
        String searchText = "";
        
        if (StringUtils.hasText(itemName)) {
            searchText += itemName.toLowerCase() + " ";
        }
        if (StringUtils.hasText(mainIngredient)) {
            searchText += mainIngredient.toLowerCase() + " ";
        }

        if (!StringUtils.hasText(searchText)) {
            return DrugCategory.GENERAL;
        }

        // 항생제 키워드
        Set<String> antibioticKeywords = Set.of(
            // 퀴놀론계
            "시프로플록사신", "ciprofloxacin", "레보플록사신", "levofloxacin",
            "옥소플록사신", "ofloxacin", "노르플록사신", "norfloxacin",
            "목시플록사신", "moxifloxacin", "게미플록사신", "gemifloxacin",
            // 페니실린계
            "아목시실린", "amoxicillin", "암피실린", "ampicillin",
            "페니실린", "penicillin", "아목시실린클라불란산", "amoxicillin-clavulanate",
            // 세팔로스포린계
            "세팔렉신", "cephalexin", "세프트리아존", "ceftriaxone",
            "세푸록심", "cefuroxime", "세파클로", "cefaclor",
            "세파돔", "cefpodoxime", "세프디닌", "cefdinir",
            // 마크로라이드계
            "아지트로마이신", "azithromycin", "클래리트로마이신", "clarithromycin",
            "에리트로마이신", "erythromycin", "로키트로마이신", "roxithromycin",
            // 테트라사이클린계
            "독시사이클린", "doxycycline", "테트라사이클린", "tetracycline",
            "미노사이클린", "minocycline",
            // 기타
            "클린다마이신", "clindamycin", "메트로니다졸", "metronidazole",
            "반코마이신", "vancomycin", "트리메토프림", "trimethoprim",
            "술파메톡사졸", "sulfamethoxazole", "항생제", "antibiotic", "항균제"
        );
        if (containsAnyKeyword(searchText, antibioticKeywords)) {
            return DrugCategory.ANTIBIOTIC;
        }

        // 수면제/진정제/항불안제 키워드
        Set<String> sleepAidKeywords = Set.of(
            // 비벤조디아제핀계 수면제
            "졸피뎀", "zolpidem", "자피클론", "zopiclone", "에스조피클론", "eszopiclone",
            "잠비엔", "ambien", "임바녹스", "imvanox",
            // 벤조디아제핀계
            "로라제팜", "lorazepam", "다이아제팜", "diazepam", "알프라졸람", "alprazolam",
            "클로나제팜", "clonazepam", "테마제팜", "temazepam", "트리아졸람", "triazolam",
            "옥사제팜", "oxazepam", "브로마제팜", "bromazepam", "니트라제팜", "nitrazepam",
            "플루니트라제팜", "flunitrazepam", "미다졸람", "midazolam",
            // 기타 수면제/진정제
            "멜라토닌", "melatonin", "수면제", "진정제", "안정제", "수면유도제",
            "sleep", "sedative", "hypnotic", "benzodiazepine", "벤조디아제핀"
        );
        if (containsAnyKeyword(searchText, sleepAidKeywords)) {
            return DrugCategory.SLEEP_AID;
        }

        // 항우울제/MAOI 키워드
        Set<String> antidepressantKeywords = Set.of(
            // SSRI (선택적 세로토닌 재흡수 억제제)
            "플루옥세틴", "fluoxetine", "설트랄린", "sertraline", "파록세틴", "paroxetine",
            "에스시탈로프람", "escitalopram", "시탈로프람", "citalopram", "플루복사민", "fluvoxamine",
            // SNRI (세로토닌-노르에피네프린 재흡수 억제제)
            "벤라팍신", "venlafaxine", "둘록세틴", "duloxetine", "밀나시프란", "milnacipran",
            // 기타 항우울제
            "부프로피온", "bupropion", "미르타자핀", "mirtazapine", "트라조돈", "trazodone",
            "아미트리프틸린", "amitriptyline", "노르트리프틸린", "nortriptyline",
            "이미프라민", "imipramine", "클로미프라민", "clomipramine",
            // MAOI
            "마오억제제", "maoi", "monoamine oxidase", "모노아민옥시다제",
            "항우울제", "antidepressant", "우울증치료제"
        );
        if (containsAnyKeyword(searchText, antidepressantKeywords)) {
            return DrugCategory.ANTIDEPRESSANT;
        }

        // 항고혈압제 키워드
        Set<String> antihypertensiveKeywords = Set.of(
            // ARB (안지오텐신 II 수용체 차단제)
            "로사르탄", "losartan", "발사르탄", "valsartan", "칸데사르탄", "candesartan",
            "올메사르탄", "olmesartan", "이르베사르탄", "irbesartan", "텔미사르탄", "telmisartan",
            "에프로사르탄", "eprosartan", "아지사르탄", "azilsartan",
            // ACE 억제제
            "리시노프릴", "lisinopril", "에날라프릴", "enalapril", "카프토프릴", "captopril",
            "라미프릴", "ramipril", "페린도프릴", "perindopril", "퀴나프릴", "quinapril",
            "벤라제프릴", "benazepril", "포시노프릴", "fosinopril", "모엑시프릴", "moexipril",
            // 칼슘 채널 차단제
            "암로디핀", "amlodipine", "니페디핀", "nifedipine", "펠로디핀", "felodipine",
            "베라파밀", "verapamil", "딜티아젬", "diltiazem", "니솔디핀", "nisoldipine",
            // 베타 차단제
            "아텐올롤", "atenolol", "메토프롤롤", "metoprolol", "프로프라놀롤", "propranolol",
            "비소프롤롤", "bisoprolol", "카르베딜롤", "carvedilol", "라베탈롤", "labetalol",
            "네비볼롤", "nebivolol", "핀돌롤", "pindolol",
            // 이뇨제
            "하이드로클로로티아지드", "hydrochlorothiazide", "푸로세미드", "furosemide",
            "스피로놀락톤", "spironolactone", "아미로라이드", "amiloride",
            "인다파미드", "indapamide", "클로르탈리돈", "chlorthalidone",
            // 기타
            "항고혈압", "고혈압", "antihypertensive", "hypertension", "혈압강하제"
        );
        if (containsAnyKeyword(searchText, antihypertensiveKeywords)) {
            return DrugCategory.ANTIHYPERTENSIVE;
        }

        // 피임약/호르몬제 키워드
        Set<String> contraceptiveKeywords = Set.of(
            // 호르몬
            "에스트로겐", "estrogen", "에스트라디올", "estradiol", "에티닐에스트라디올", "ethinyl estradiol",
            "프로게스테론", "progesterone", "프로게스틴", "progestin", "레보노르게스트렐", "levonorgestrel",
            "노르게스트렐", "norgestrel", "데소게스트렐", "desogestrel", "드로스피레논", "drospirenone",
            "게스토덴", "gestodene", "노르게스트마테", "norgestimate", "디에노게스트", "dienogest",
            // 피임약
            "피임약", "호르몬", "경구피임약", "oral contraceptive", "피임", "contraceptive",
            "birth control", "호르몬제", "hormone", "hormonal", "에스트로겐제", "프로게스테론제",
            // 호르몬 대체 요법
            "HRT", "hormone replacement", "호르몬대체요법"
        );
        if (containsAnyKeyword(searchText, contraceptiveKeywords)) {
            return DrugCategory.CONTRACEPTIVE;
        }

        // 진통제 키워드 (일반으로 분류)
        Set<String> painkillerKeywords = Set.of(
            "타이레놀", "tylenol", "아세트아미노펜", "acetaminophen", "파라세타몰", "paracetamol",
            "이부프로펜", "ibuprofen", "아스피린", "aspirin", "나프록센", "naproxen",
            "디클로페낙", "diclofenac", "케토롤락", "ketorolac", "인도메타신", "indomethacin",
            "진통제", "painkiller", "analgesic", "해열진통제"
        );
        if (containsAnyKeyword(searchText, painkillerKeywords)) {
            return DrugCategory.GENERAL;
        }

        // 비타민/영양제 키워드 (일반으로 분류)
        Set<String> vitaminKeywords = Set.of(
            "비타민", "vitamin", "비타민C", "vitamin C", "비타민D", "vitamin D",
            "비타민B", "vitamin B", "엽산", "folic acid", "칼슘", "calcium",
            "철분", "iron", "마그네슘", "magnesium", "오메가3", "omega-3",
            "영양제", "supplement", "건강보조식품"
        );
        if (containsAnyKeyword(searchText, vitaminKeywords)) {
            return DrugCategory.GENERAL;
        }

        return DrugCategory.GENERAL;
    }

    /**
     * 검색 텍스트에 키워드가 포함되어 있는지 확인합니다.
     */
    private boolean containsAnyKeyword(String searchText, Set<String> keywords) {
        return keywords.stream().anyMatch(searchText::contains);
    }
}

