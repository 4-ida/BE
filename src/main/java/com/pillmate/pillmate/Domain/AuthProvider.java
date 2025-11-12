package com.pillmate.pillmate.Domain;

import lombok.Getter;

@Getter
public enum AuthProvider {
    LOCAL("일반 가입"),
    GOOGLE("구글"),
    KAKAO("카카오"),
    NAVER("네이버"),
    APPLE("애플");

    private final String description;

    AuthProvider(String description) {
        this.description = description;
    }
}



