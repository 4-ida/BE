package com.pillmate.pillmate.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String accessToken;
    private String tokenType;
    private Long expiresInMillis;
    private UserInfo user;
    private boolean firstLogin;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String name;
    }
}

