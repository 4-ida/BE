package com.pillmate.pillmate.Security.OAuth2;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OAuth2UserProviderRouter implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    
    private final GoogleOAuth2UserService googleOAuth2UserService;
    // 추후 NaverOAuth2UserService, KakaoOAuth2UserService 등 추가 가능
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        
        return switch (provider) {
            case "google" -> googleOAuth2UserService.loadUser(userRequest);
            // case "naver" -> naverOAuth2UserService.loadUser(userRequest);
            // case "kakao" -> kakaoOAuth2UserService.loadUser(userRequest);
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + provider);
        };
    }
}

