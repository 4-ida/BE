package com.pillmate.pillmate.Domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = @UniqueConstraint(name = "uk_bookmark_user_drug", columnNames = {"userId", "drugId"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 사용자 식별자 (JWT에서 꺼낸 값)
    @Column(nullable = false)
    private Long userId;

    // 약 ID (예: DRUG-000123)
    @Column(nullable = false, length = 32)
    private String drugId;

    @Column(nullable = false)
    private Instant bookmarkedAt; // UTC 권장
}
