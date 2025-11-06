package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.Drug;
import com.pillmate.pillmate.Repository.DrugRepository;
import com.pillmate.pillmate.DTO.SuggestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;

    public SuggestResponse suggest(String rawQuery, Integer limit) {
        String q = rawQuery == null ? "" : rawQuery.trim();
        if (q.length() < 2) {
            return SuggestResponse.empty(rawQuery); // 2자 미만이면 빈 결과
        }

        int max = (limit == null || limit <= 0) ? 10 : Math.min(limit, 20);

        // 단계별 조회: Exact > Prefix > Substring
        List<Drug> exact = drugRepository.findExact(q);
        List<Drug> prefix = drugRepository.findPrefix(q);
        List<Drug> sub    = drugRepository.findSubstring(q);

        // 중복 제거 + 순서 보존
        LinkedHashMap<String, Drug> ordered = new LinkedHashMap<>();
        for (Drug d : exact)   ordered.putIfAbsent(d.getId(), d);
        for (Drug d : prefix)  ordered.putIfAbsent(d.getId(), d);
        for (Drug d : sub)     ordered.putIfAbsent(d.getId(), d);

        // 상위 limit 개만 변환
        List<SuggestResponse.Suggestion> suggestions = new ArrayList<>();
        for (Drug d : ordered.values()) {
            if (suggestions.size() >= max) break;
            String label = makeLabel(d); // "제품명 – 성분명" 형태
            suggestions.add(new SuggestResponse.Suggestion(label, d.getId()));
        }

        return new SuggestResponse(rawQuery, suggestions);
    }

    private String makeLabel(Drug d) {
        String name = Optional.ofNullable(d.getName()).orElse("");
        String generic = Optional.ofNullable(d.getGenericName()).orElse("");
        if (!generic.isBlank()) return name + " – " + generic;
        return name;
    }
}
