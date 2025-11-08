package com.pillmate.pillmate.DTO;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BookmarkListResponse {

    private int page;              // 현재 페이지
    private int size;              // 페이지 크기
    private String sort;           // 정렬 기준 (recent)
    private long totalElements;    // 총 개수
    private List<Item> items;      // 항목들

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private String drugId;
        private String name;
        private String thumbnailUrl;
        private String bookmarkedAt; // ISO-8601 "…Z"
    }
}
