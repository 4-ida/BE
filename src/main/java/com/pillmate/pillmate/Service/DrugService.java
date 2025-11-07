package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.Drug;
import com.pillmate.pillmate.Repository.DrugRepository;
import com.pillmate.pillmate.DTO.SuggestResponse;
import com.pillmate.pillmate.DTO.SearchResponse;
import com.pillmate.pillmate.DTO.ImageResponse;
import com.pillmate.pillmate.DTO.DrugInfoResponse;
import com.pillmate.pillmate.DTO.InteractionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;
    private final TextNormalizer textNormalizer;

    /* -------------------- 약 명 자동완성 -------------------- */
    public SuggestResponse suggest(String rawQuery, Integer limit) {
        String q = rawQuery == null ? "" : rawQuery.trim();
        if (q.length() < 2) return SuggestResponse.empty(rawQuery);

        int max = (limit == null || limit <= 0) ? 10 : Math.min(limit, 20);

        List<Drug> exact  = drugRepository.findExact(q);
        List<Drug> prefix = drugRepository.findPrefix(q);
        List<Drug> sub    = drugRepository.findSubstring(q);

        LinkedHashMap<String, Drug> ordered = new LinkedHashMap<>();
        for (Drug d : exact)  ordered.putIfAbsent(d.getId(), d);
        for (Drug d : prefix) ordered.putIfAbsent(d.getId(), d);
        for (Drug d : sub)    ordered.putIfAbsent(d.getId(), d);

        List<SuggestResponse.Suggestion> suggestions = new ArrayList<>();
        for (Drug d : ordered.values()) {
            if (suggestions.size() >= max) break;
            suggestions.add(new SuggestResponse.Suggestion(d.getName(), d.getId()));
        }
        return new SuggestResponse(rawQuery, suggestions);
    }

    /* -------------------- 약 명 검색 -------------------- */
    public SearchResponse search(String rawQ, Integer page, Integer size) {
        if (!StringUtils.hasText(rawQ)) throw new IllegalArgumentException("q must not be empty");
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0 || size > 100) ? 20 : size;

        String qNorm = textNormalizer.normalize(rawQ);
        Page<Drug> pageResult =
                drugRepository.searchByNameRelevance(qNorm.toLowerCase(), PageRequest.of(p, s));

        return SearchResponse.builder()
                .query(SearchResponse.Query.builder().q(rawQ).build())
                .page(p)
                .size(s)
                .totalElements(pageResult.getTotalElements())
                .items(pageResult.getContent().stream().map(this::toSearchItem).toList())
                .build();
    }

    private SearchResponse.Item toSearchItem(Drug d) {
        return SearchResponse.Item.builder()
                .drugId(d.getId())
                .name(d.getName())
                .ingredient(d.getIngredients())
                .form(d.getForm())
                .strength(d.getStrength())
                .build();
    }

    /* ==================== [추가] 상세 3종 ==================== */

    // (1) 이미지 조회
    public ImageResponse getDrugImages(String drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약물입니다."));

        // CDN 규칙(임시): front/back 없으면 썸네일/백 이미지로 구성
        String baseId = drug.getId(); // DRUG-000123
        String front = (drug.getThumbnailUrl() != null && !drug.getThumbnailUrl().isBlank())
                ? drug.getThumbnailUrl()
                : "https://cdn.pillmate.com/img/" + baseId + "-front.png";
        String back  = "https://cdn.pillmate.com/img/" + baseId + "-back.png";

        List<ImageResponse.Image> imgs = List.of(
                ImageResponse.Image.builder().type("front").url(front).build(),
                ImageResponse.Image.builder().type("back").url(back).build()
        );

        // meta는 추후 테이블 생기면 실제 값으로 교체
        ImageResponse.Meta meta = ImageResponse.Meta.builder()
                .color("white")
                .shape("원형")
                .imprint(null) // 각인 데이터가 없으므로 일단 null
                .build();

        return ImageResponse.builder()
                .drugId(drugId)
                .images(imgs)
                .meta(meta)
                .build();
    }

    // (2) 기본 정보 조회
    public DrugInfoResponse getDrugInfo(String drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약물입니다."));

        // caution / warnings 은 추후 DB화. 지금은 기본값/템플릿.
        DrugInfoResponse.Caution caution = DrugInfoResponse.Caution.builder()
                .alcohol("복용 전후 12시간 음주 금지")
                .caffeine("복용 전후 6시간 카페인 섭취 자제")
                .build();

        List<String> warnings = List.of(
                "간 질환자 복용 전 의사 상담",
                "과량 복용 시 간 손상 위험"
        );

        return DrugInfoResponse.builder()
                .drugId(drugId)
                .name(drug.getName())
                .ingredient(drug.getIngredients())
                .form(drug.getForm())
                .strength(drug.getStrength())
                .rxType("일반의약품")               // 추후 필드 생기면 대체
                .caution(caution)
                .warnings(warnings)
                .bookmarked(true)                  // 북마크 화면에서 진입했다고 가정
                .build();
    }
    // (2) 기본 정보 조회
    public DrugInfoResponse getDrugInfo(String drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약물입니다."));

        // caution / warnings 은 추후 DB화. 지금은 기본값/템플릿.
        DrugInfoResponse.Caution caution = DrugInfoResponse.Caution.builder()
                .alcohol("복용 전후 12시간 음주 금지")
                .caffeine("복용 전후 6시간 카페인 섭취 자제")
                .build();

        List<String> warnings = List.of(
                "간 질환자 복용 전 의사 상담",
                "과량 복용 시 간 손상 위험"
        );

        return DrugInfoResponse.builder()
                .drugId(drugId)
                .name(drug.getName())
                .ingredient(drug.getIngredients())
                .form(drug.getForm())
                .strength(drug.getStrength())
                .rxType("일반의약품")               // 추후 필드 생기면 대체
                .caution(caution)
                .warnings(warnings)
                .bookmarked(true)                  // 북마크 화면에서 진입했다고 가정
                .build();
    }

    // (3) 상호작용 조회
    public InteractionResponse getDrugInteractions(String drugId) {
        Drug drug = drugRepository.findById(drugId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약물입니다."));

        // 기본 정책값(카페인 6h, 알코올 12h)
        InteractionResponse.SafeWindow caffeineWin =
                InteractionResponse.SafeWindow.builder()
                    .beforeMinutes(360).afterMinutes(360).build();

        InteractionResponse.SafeWindow alcoholWin =
                InteractionResponse.SafeWindow.builder()
                    .beforeMinutes(720).afterMinutes(720).build();

        List<InteractionResponse.Beverage> beverage = List.of(
                InteractionResponse.Beverage.builder()
                        .target("카페인")
                        .recommendation("복용 전후 6시간 카페인 섭취 자제")
                        .safeWindow(caffeineWin)
                        .build(),
                InteractionResponse.Beverage.builder()
                        .target("알코올")
                        .recommendation("복용 전후 12시간 음주 금지")
                        .safeWindow(alcoholWin)
                        .build()
        );

        InteractionResponse.Interactions interactions =
                InteractionResponse.Interactions.builder()
                        .beverage(beverage)
                        .build();

        return InteractionResponse.builder()
                .drugId(drug.getId())
                .interactions(interactions)
                .build();
    }

    

}


