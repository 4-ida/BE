package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.Drug;
import com.pillmate.pillmate.Repository.DrugRepository;
import com.pillmate.pillmate.DTO.SuggestResponse;
import com.pillmate.pillmate.DTO.SearchResponse;
import com.pillmate.pillmate.DTO.ImageResponse;
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

    /* ==================== [추가] 상세 2종 ==================== */

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

    // (2) 상호작용 조회
    public InteractionResponse getDrugInteractions(String drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약물입니다."));

        InteractionResponse.SafeWindow caffeineWin = new InteractionResponse.SafeWindow();
        caffeineWin.setBeforeMinutes(360);
        caffeineWin.setAfterMinutes(360);

        InteractionResponse.SafeWindow alcoholWin = new InteractionResponse.SafeWindow();
        alcoholWin.setBeforeMinutes(720);
        alcoholWin.setAfterMinutes(720);

        InteractionResponse.Beverage caffeine = new InteractionResponse.Beverage();
        caffeine.setTarget("카페인");
        caffeine.setRecommendation("복용 전후 6시간 카페인 섭취 자제");
        caffeine.setSafeWindow(caffeineWin);

        InteractionResponse.Beverage alcohol = new InteractionResponse.Beverage();
        alcohol.setTarget("알코올");
        alcohol.setRecommendation("복용 전후 12시간 음주 금지");
        alcohol.setSafeWindow(alcoholWin);

        InteractionResponse.Interactions inter = new InteractionResponse.Interactions();
        inter.setBeverage(java.util.List.of(caffeine, alcohol));

        InteractionResponse res = new InteractionResponse();
        res.setDrugId(drug.getId());
        res.setInteractions(inter);
        return res;
    }
}


