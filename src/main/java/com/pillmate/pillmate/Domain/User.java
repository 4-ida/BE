package com.pillmate.pillmate.Domain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(length = 255)
    private String profileImage;

    @Column(length = 20)
    private String caffeineSensitivity;

    @Column(length = 20)
    private String drinkingPattern;

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
    public void updateProfile(String name, String profileImage, String caffeineSensitivity, String drinkingPattern) {
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
}
