package com.pillmate.pillmate.Domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 10000)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CaffeineSensitivity caffeineSensitivity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DrinkingPattern drinkingPattern;

    private Double defaultCaffeineAmount;

    // 기본 알코올 함량 (g 또는 % 중에서 프런트가 맞춰줄 값)
    private Double defaultAlcoholAmount;

    // 현재 복용 중인 약 (콤마로 적거나 그냥 문장으로)
    @Column(length = 500)
    private String currentMedications;

    // 섭취 음료 종류 드롭다운에서 마지막에 선택한 값
    // 예: "소주", "맥주", "에너지 드링크", "커피", "직접 입력"
    @Column(length = 50)
    private String preferredBeverageType;

    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean termsOfService;  // 서비스 이용약관 동의 여부
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean privacyPolicy;  // 개인정보 처리방침 동의 여부
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean dataUsage;  // 데이터 활용 동의 여부
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'LOCAL'")
    private AuthProvider provider;  // 인증 제공자 (LOCAL, GOOGLE, KAKAO, NAVER, APPLE)
    
    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100) DEFAULT 'LOCAL'")
    private String providerId;  // 제공자별 고유 ID
    
    @CreatedDate
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime lastLoginAt;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer loginCount = 0;

    // 비밀번호 암호화 메서드
    public void encodePassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.password = encoder.encode(rawPassword);
    }
    
    // 비밀번호 검증 메서드
    public boolean checkPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, this.password);
    }
    public void updateProfile(String name,
        String profileImage,
        CaffeineSensitivity caffeineSensitivity,
        DrinkingPattern drinkingPattern) {
        if (name != null) this.name = name;
        if (profileImage != null) this.profileImage = profileImage;
        if (caffeineSensitivity != null) this.caffeineSensitivity = caffeineSensitivity;
        if (drinkingPattern != null) this.drinkingPattern = drinkingPattern;
    }
    public void updateBasicProfile(Double caffeine, Double alcohol, String meds, String beverage) {
        if (caffeine != null) this.defaultCaffeineAmount = caffeine;
        if (alcohol != null) this.defaultAlcoholAmount = alcohol;
        if (meds != null) this.currentMedications = meds;
        if (beverage != null) this.preferredBeverageType = beverage;
    }
    
    // 소셜 로그인 사용자 생성 메서드
    public static User createSocialUser(String email, String name, AuthProvider provider, String providerId) {
        User user = new User();
        // 이메일 정규화 (trim, 소문자 변환)
        user.email = email == null ? null : email.trim().toLowerCase();
        user.password = "oauth2"; // 소셜 로그인은 비밀번호 불필요
        user.name = name;
        user.provider = provider;
        user.providerId = providerId;
        user.termsOfService = true;  // 소셜 로그인 시 약관 동의로 간주
        user.privacyPolicy = true;
        user.dataUsage = false;  // 선택 약관은 false
        user.loginCount = 0;
        return user;
    }

    public boolean markLogin() {
        boolean isFirst = loginCount == null || loginCount == 0;
        if (loginCount == null) {
            loginCount = 0;
        }
        loginCount += 1;
        lastLoginAt = LocalDateTime.now();
        return isFirst;
    }
}
