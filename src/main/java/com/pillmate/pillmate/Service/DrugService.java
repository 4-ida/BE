package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.Drug;
import com.pillmate.pillmate.Repository.DrugRepository;
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse;
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse.MfdsEasyDrugItem;
import com.pillmate.pillmate.DTO.SuggestResponse;
import com.pillmate.pillmate.DTO.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;
    private final TextNormalizer textNormalizer;
    private final MfdsDrugInfoClient mfdsDrugInfoClient; 

    /* -------------------- 약 명 자동완성 -------------------- */
    public SuggestResponse suggest(String rawQuery, Integer limit) {
        // 0) 입력 전처리 및 검증
        String q = (rawQuery == null) ? "" : rawQuery.trim();
        if (!StringUtils.hasText(q)) {
            return SuggestResponse.empty(rawQuery); 
        }
        String qNorm = textNormalizer.normalize(q);
        if (qNorm.length() < 2) {
            // 최소 길이 미만이면 빈 결과
            return SuggestResponse.empty(rawQuery);
        }
        int max = (limit == null || limit <= 0) ? 10 : Math.min(limit, 20);

        // 1) 식약처 OPEN API 호출 (쉬운약 검색)
        List<MfdsEasyDrugItem> mfdsItems = mfdsDrugInfoClient
                .searchByName(q, 0, max) // page=0, size=max
                .map(res -> {
                    var body = res.getBody();
                    return (body == null || body.getItems() == null)
                            ? List.<MfdsEasyDrugItem>of()
                            : body.getItems();
                })
                .orElseGet(List::of);

        // 2) 간단 유사도 정렬(정확==0, 접두==1, 포함==2, 기타==3)
        Comparator<MfdsEasyDrugItem> bySimilarity = Comparator
                .comparingInt((MfdsEasyDrugItem it) -> {
                    String nameNorm = textNormalizer.normalize(nullToEmpty(it.getItemName()));
                    if (nameNorm.equals(qNorm)) return 0;
                    if (nameNorm.startsWith(qNorm)) return 1;
                    if (nameNorm.contains(qNorm)) return 2;
                    return 3;
                })
                .thenComparing(it -> nullToEmpty(it.getItemName()).length());

        List<MfdsEasyDrugItem> sorted = mfdsItems.stream()
                .sorted(bySimilarity)
                .limit(max)
                .toList();

        // 3) SuggestResponse로 매핑 (label=itemName, value=itemSeq)
        List<SuggestResponse.Suggestion> suggestions = sorted.stream()
                .map(it -> new SuggestResponse.Suggestion(
                        nullToEmpty(it.getItemName()),
                        nullToEmpty(it.getItemSeq())
                ))
                .collect(Collectors.toList());

        return new SuggestResponse(rawQuery, suggestions);
    }

    private String nullToEmpty(String s) { return (s == null) ? "" : s; }


    /* -------------------- 약 명 검색 -------------------- */
     /** 약 명 검색 (MFDS + 내부 캐시 혼합, 최소 필드 응답) */
    public SearchResponse search(String rawQ, Integer page, Integer size) {
        try {
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
            List<SearchResponse.Item> fromDb = List.of();
            try {
                fromDb = drugRepository
                        .searchByNameRelevance(qNorm.toLowerCase(), PageRequest.of(p, s))
                        .getContent()
                        .stream()
                        .map(this::mapDrugToSearchItem)
                        .filter(item -> item != null && item.getDrugId() != null) // null 필터링
                        .toList();
            } catch (Exception ex) {
                log.warn("Failed to search from DB for q={}", rawQ, ex);
                // DB 검색 실패해도 계속 진행
            }

            // -------- 2) MFDS OpenAPI 검색 --------
            List<SearchResponse.Item> fromMfds = List.of();
            try {
                fromMfds = mfdsDrugInfoClient
                        .searchByName(rawQ, p, s)  // 원문 기준으로도 검색
                        .map(this::mapMfdsToItems)
                        .orElseGet(List::of);
            } catch (Exception ex) {
                log.warn("Failed to search from MFDS for q={}", rawQ, ex);
                // MFDS 검색 실패해도 계속 진행
            }

            // -------- 3) 병합 + 유사도 정렬 + 중복 제거 --------
            // 우선 내부 DB → MFDS 순으로 합치고 drugId로 중복 제거
            LinkedHashMap<String, SearchResponse.Item> merged = new LinkedHashMap<>();

            // 두 소스 결합 (우선순위: DB 먼저)
            for (SearchResponse.Item it : fromDb) {
                if (it != null && it.getDrugId() != null) {
                    merged.putIfAbsent(it.getDrugId(), it);
                }
            }
            for (SearchResponse.Item it : fromMfds) {
                if (it != null && it.getDrugId() != null) {
                    merged.putIfAbsent(it.getDrugId(), it);
                }
            }

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
        } catch (IllegalArgumentException ex) {
            log.error("Invalid search parameter: q={}, page={}, size={}", rawQ, page, size, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during drug search: q={}, page={}, size={}", rawQ, page, size, ex);
            throw new RuntimeException("Drug search failed: " + ex.getMessage(), ex);
        }
    }

    /** 내부 Drug → 최소 필드 Item 매핑 */
    private SearchResponse.Item mapDrugToSearchItem(Drug d) {
        if (d == null) {
            return null;
        }
        try {
            return SearchResponse.Item.builder()
                    .drugId(d.getId())             // 내부 스냅샷의 id가 MFDS itemSeq와 동일하다는 가정
                    .name(d.getName())
                    .thumbnailUrl(d.getThumbnailUrl())
                    .build();
        } catch (Exception ex) {
            log.warn("Failed to map Drug to SearchItem: drugId={}", d.getId(), ex);
            return null;
        }
    }

    /** MFDS 응답 → 최소 필드 Item 리스트 매핑 */
    private List<SearchResponse.Item> mapMfdsToItems(MfdsEasyDrugResponse res) {
        if (res == null) {
            return List.of();
        }
        try {
            if (res.getBody() == null || res.getBody().getItems() == null) {
                return List.of();
            }
            return res.getBody().getItems().stream()
                    .filter(it -> it != null) // null 아이템 필터링
                    .map(it -> {
                        try {
                            return SearchResponse.Item.builder()
                                    .drugId(safeStr(it.getItemSeq()))
                                    .name(safeStr(it.getItemName()))
                                    .thumbnailUrl(safeStr(it.getItemImage()))
                                    .build();
                        } catch (Exception ex) {
                            log.warn("Failed to map MfdsEasyDrugItem to SearchItem: itemSeq={}", it.getItemSeq(), ex);
                            return null;
                        }
                    })
                    .filter(item -> item != null && item.getDrugId() != null) // null 필터링
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Failed to map MFDS response to items", ex);
            return List.of();
        }
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
}


