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
        private String drugId;
        private String name;
        private List<String> ingredient; // 주성분 리스트
        private String form;             // 제형
        private String strength;         // 함량
    }

    private Query query;
    private int page;
    private int size;
    private long totalElements;
    private List<Item> items;
}

