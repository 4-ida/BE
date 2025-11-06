package com.pillmate.pillmate.Domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drugs",
       indexes = {
         @Index(name = "ix_drugs_name", columnList = "name"),
         @Index(name = "ix_drugs_generic_name", columnList = "genericName"),
         @Index(name = "ix_drugs_normalized_name", columnList = "normalizedName")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Drug {

    @Id
    @Column(length = 32)
    private String id;               // 예: DRUG-000123

    @Column(nullable = false)
    private String name;             // 제품명 (예: 타이레놀 500mg)

    private String genericName;      // 성분명 (예: Acetaminophen)

    @Column(nullable = false)
    private String normalizedName;   // 검색용 정규화 문자열(소문자/공백,기호 제거)

    private Boolean active;          // 판매/유통 여부
    private Integer popularityScore; // 선택/조회 가점(없으면 null/0)

    private String thumbnailUrl;     // 목록에서 쓰는 대표 이미지
}
