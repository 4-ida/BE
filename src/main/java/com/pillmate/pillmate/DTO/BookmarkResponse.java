package com.pillmate.pillmate.DTO;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BookmarkResponse {
    private String drugId;        // DRUG-000123
    private String name;          // 약 이름
    private String thumbnailUrl;  // 썸네일
    private String bookmarkedAt;  // ISO-8601 UTC 문자열
}
