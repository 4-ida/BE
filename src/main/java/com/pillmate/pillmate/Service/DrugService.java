package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.Drug;
import com.pillmate.pillmate.Repository.DrugRepository;
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse;
import com.pillmate.pillmate.DTO.SuggestResponse;
import com.pillmate.pillmate.DTO.SearchResponse;
import com.pillmate.pillmate.DTO.ImageResponse;
import com.pillmate.pillmate.DTO.InteractionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;
    private final TextNormalizer textNormalizer;
    private final MfdsDrugInfoClient mfdsDrugInfoClient; 

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
     /** 약 명 검색 (MFDS + 내부 캐시 혼합, 최소 필드 응답) */
    public SearchResponse search(String rawQ, Integer page, Integer size) {
        if (!StringUtils.hasText(rawQ)) {
            throw new IllegalArgumentException("q must not be empty");
        }
        String qNorm = textNormalizer.normalize(rawQ);
        if (qNorm.length() < 2) {
            throw new IllegalArgumentException("q must be at least 2 characters");
        }

        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 10 : Math.min(size, 20);

        // -------- 1) 내부 DB 스냅샷(선택) --------
        // 내부 DB가 동기화된 품목을 일부 갖고 있다면 우선 가져오기
        // NOTE: 만약 내부 스냅샷이 없다면 이 블록을 제거해도 됨
        List<SearchResponse.Item> fromDb = drugRepository
                .searchByNameRelevance(qNorm.toLowerCase(), PageRequest.of(p, s))
                .getContent()
                .stream()
                .map(this::mapDrugToSearchItem)
                .toList();

        // -------- 2) MFDS OpenAPI 검색 --------
        List<SearchResponse.Item> fromMfds = mfdsDrugInfoClient
                .searchByName(rawQ, p, s)  // 원문 기준으로도 검색
                .map(this::mapMfdsToItems)
                .orElseGet(List::of);

        // -------- 3) 병합 + 유사도 정렬 + 중복 제거 --------
        // 우선 내부 DB → MFDS 순으로 합치고 drugId로 중복 제거
        LinkedHashMap<String, SearchResponse.Item> merged = new LinkedHashMap<>();

        // 두 소스 결합 (우선순위: DB 먼저)
        for (SearchResponse.Item it : fromDb)     merged.putIfAbsent(it.getDrugId(), it);
        for (SearchResponse.Item it : fromMfds)   merged.putIfAbsent(it.getDrugId(), it);

        // 유사도 정렬 (접두 > 단어경계 포함 > 일반 포함)
        List<SearchResponse.Item> sorted = merged.values()
                .stream()
                .sorted(Comparator
                        .comparingInt((SearchResponse.Item it) -> rankBySimilarity(qNorm, it.getName()))
                        .thenComparing(it -> it.getName() == null ? 999 : it.getName().length()))
                .toList();

        // MFDS의 전체 count를 믿는 정책도 가능하지만, 병합했으니 일단 merged 크기로 표시
        long total = merged.size();

        return SearchResponse.builder()
                .query(SearchResponse.Query.builder().q(rawQ).build())
                .page(p)
                .size(s)
                .totalElements(total)
                .items(sorted)
                .build();
    }

    /** 내부 Drug → 최소 필드 Item 매핑 */
    private SearchResponse.Item mapDrugToSearchItem(Drug d) {
        return SearchResponse.Item.builder()
                .drugId(d.getId())             // 내부 스냅샷의 id가 MFDS itemSeq와 동일하다는 가정
                .name(d.getName())
                .thumbnailUrl(d.getThumbnailUrl())
                .build();
    }

    /** MFDS 응답 → 최소 필드 Item 리스트 매핑 */
    private List<SearchResponse.Item> mapMfdsToItems(MfdsEasyDrugResponse res) {
        if (res.getBody() == null || res.getBody().getItems() == null) return List.of();
        return res.getBody().getItems().stream()
                .map(it -> SearchResponse.Item.builder()
                        .drugId(safeStr(it.getItemSeq()))
                        .name(safeStr(it.getItemName()))
                        .thumbnailUrl(safeStr(it.getItemImage())) // 필드명(it.getItemImage)이 다르면 레포 구조에 맞게 수정
                        .build())
                .collect(Collectors.toList());
    }

    private String safeStr(String s) { return (s == null || s.isBlank()) ? null : s; }

    /**
     * 간단 유사도 랭크: 접두(0) > 단어경계 포함(1) > 일반 포함(2) > 그 외(3)
     * 값이 낮을수록 유사도가 높음.
     */
    private int rankBySimilarity(String qNorm, String name) {
        if (name == null) return 3;
        String n = textNormalizer.normalize(name);
        if (n.startsWith(qNorm)) return 0;
        if (n.contains(" " + qNorm)) return 1;
        if (n.contains(qNorm)) return 2;
        return 3;
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


