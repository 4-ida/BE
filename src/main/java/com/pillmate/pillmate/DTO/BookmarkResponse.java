package com.pillmate.pillmate.DTO;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BookmarkResponse {
    private String drugId;        // DRUG-000123
    private String bookmarkedAt;  // ISO-8601 UTC 문자열
}
