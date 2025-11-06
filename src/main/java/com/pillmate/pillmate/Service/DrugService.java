package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.Drug;
import com.pillmate.pillmate.Repository.DrugRepository;
import com.pillmate.pillmate.DTO.SuggestResponse;
import com.pillmate.pillmate.DTO.SearchResponse;
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

    /* -------------------- 약명 자동완성 -------------------- */
    public SuggestResponse suggest(String rawQuery, Integer limit) {
        String q = rawQuery == null ? "" : rawQuery.trim();
        if (q.length() < 2) {
            return SuggestResponse.empty(rawQuery); // 2자 미만이면 빈 결과
        }

        int max = (limit == null || limit <= 0) ? 10 : Math.min(limit, 20);

        // 단계별 조회: Exact > Prefix > Substring
        // repo 쿼리 자체가 lower(...) 처리하므로 그대로 전달해도 OK
        List<Drug> exact = drugRepository.findExact(q);
        List<Drug> prefix = drugRepository.findPrefix(q);
        List<Drug> sub    = drugRepository.findSubstring(q);

        // 중복 제거 + 순서 보존
        LinkedHashMap<String, Drug> ordered = new LinkedHashMap<>();
        for (Drug d : exact)   ordered.putIfAbsent(d.getId(), d);
        for (Drug d : prefix)  ordered.putIfAbsent(d.getId(), d);
        for (Drug d : sub)     ordered.putIfAbsent(d.getId(), d);

        // 상위 limit 개만 변환 (label은 스펙대로 '제품명'만)
        List<SuggestResponse.Suggestion> suggestions = new ArrayList<>();
        for (Drug d : ordered.values()) {
            if (suggestions.size() >= max) break;
            suggestions.add(new SuggestResponse.Suggestion(d.getName(), d.getId()));
        }

        return new SuggestResponse(rawQuery, suggestions);
    }

    /* -------------------- 약명 검색 -------------------- */
    public SearchResponse search(String rawQ, Integer page, Integer size) {
        if (!StringUtils.hasText(rawQ)) {
            throw new IllegalArgumentException("q must not be empty");
        }

        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0 || size > 100) ? 20 : size;

        // 전처리(공백/기호/단위 정리 등) — 쿼리에는 소문자 적용
        String qNorm = textNormalizer.normalize(rawQ);
        Page<Drug> pageResult =
                drugRepository.searchByNameRelevance(qNorm.toLowerCase(), PageRequest.of(p, s));

        return SearchResponse.builder()
                .query(SearchResponse.Query.builder().q(rawQ).build()) // 원문 그대로 표기
                .page(p)
                .size(s)
                .totalElements(pageResult.getTotalElements())
                .items(pageResult.getContent().stream()
                        .map(this::toSearchItem)
                        .toList())
                .build();
    }

    private SearchResponse.Item toSearchItem(Drug d) {
        return SearchResponse.Item.builder()
                .drugId(d.getId())
                .name(d.getName())
                .ingredient(d.getIngredients()) //  엔티티 변환기 그대로 사용
                .form(d.getForm())
                .strength(d.getStrength())
                .build();
    }
}

