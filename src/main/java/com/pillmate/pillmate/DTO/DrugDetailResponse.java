package com.pillmate.pillmate.DTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.*;
import java.time.Instant;

@Getter  @Setter
@NoArgsConstructor @Builder
@AllArgsConstructor
public class DrugDetailResponse {
    private String drugId;                 // item_seq
    private String name;                   // 제품명
    private String entpName;               // 제조/수입사명
    private String rxType;                 // 전문/일반
    private String strength;               // 성분명 + 함량
    private List<String> ingredients;      // 성분
    private String efficacy;               // 효능·효과
    private String dosage;                 // 용법·용량
    private String cautions;               // 주의사항 전문
    private Map<String, String> cautionsSummary; // 요약
    private List<String> images;           // 이미지 URL들
    private Instant retrievedAt; 
    // private final String drugId;
    // private final String name;
    // private final String entpName;
    // private final String rxType;
    // private final String strength;
    // private final List<String> ingredients;
    // private final String efficacy;
    // private final String dosage;
    // private final String cautions;
    // private final Map<String, String> cautionsSummary;
    // private final List<String> images;
    // private final OffsetDateTime retrievedAt;
}

