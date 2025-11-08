package com.pillmate.pillmate.DTO;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BookmarkDeleteResponse {
    private boolean deleted;  // 삭제 여부
    private String drugId;    // 삭제된 약 ID
    private String deletedAt; // ISO-8601 (UTC)
}
