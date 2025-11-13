package com.pillmate.pillmate.DTO;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SearchResponse {

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Query {
        private String q;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private String drugId;       // 식약처 품목기준코드 (itemSeq)
        private String name;         // 제품명
        private String thumbnailUrl; // 함량
        private boolean bookmarked;
    }

    private Query query;
    private int page;
    private int size;
    private long totalElements;
    private List<Item> items;
}

