package com.pillmate.pillmate.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@Table(
    name = "drugs",
    indexes = {
        @Index(name = "ix_drugs_name",           columnList = "name"),
        @Index(name = "ix_drugs_generic_name",   columnList = "genericName"),
        @Index(name = "ix_drugs_normalized_name",columnList = "normalizedName")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Drug {

    @Id
    @Column(length = 32)
    private String id;                // 예: DRUG-000123

    @Column(nullable = false)
    private String name;              // 제품명 (예: 타이레놀 500mg)

    private String genericName;       // 성분명(일반명) (예: Acetaminophen)

    @Column(nullable = false)
    private String normalizedName;    // 검색용 정규화 문자열

    @Builder.Default
    private Boolean active = true;    // 판매/유통 여부(기본 true)

    private Integer popularityScore;  // 정렬 가중치(없으면 null/0)

    private String thumbnailUrl;      // 대표 이미지(목록/상세에서 사용 가능)

    /* ▼ 검색 응답에 필요한 필드들(서비스에서 getForm/getStrength/getIngredients 호출) ▼ */

    /** 제형(정제/캡슐 등) */
    private String form;

    /** 함량(예: 500mg) */
    private String strength;

    /**
     * 주성분을 CSV로 저장(테이블 추가 없이 안전).
     * 예: "Acetaminophen, Caffeine"
     */
    @Column(name = "ingredients_csv", length = 1000)
    private String ingredientsCsv;

    /**
     * 서비스/DTO에서 기대하는 getter.
     * CSV를 List<String>으로 변환해서 반환합니다.
     */
    public List<String> getIngredients() {
        if (ingredientsCsv == null || ingredientsCsv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(ingredientsCsv.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .toList();
    }

    /* (선택) 과거 코드 호환을 위한 편의 getter */
    public String getDrugId() { return this.id; }
}
