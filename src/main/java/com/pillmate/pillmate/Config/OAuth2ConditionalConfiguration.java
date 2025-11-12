package com.pillmate.pillmate.Config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 클라이언트 설정을 조건부로 생성
 * 환경 변수가 있을 때만 OAuth2 클라이언트를 생성합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OAuth2ConditionalConfiguration {
    
    private final Environment environment;
    
    /**
     * 환경 변수가 있을 때만 OAuth2 클라이언트 등록 리포지토리를 생성합니다.
     * 환경 변수가 없으면 null을 반환하여 빈을 생성하지 않습니다.
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();
        
        // 환경 변수 디버깅 로그
        log.info("=== OAuth2 환경 변수 확인 ===");
        String googleClientId = environment.getProperty("GOOGLE_CLIENT_ID");
        String googleClientSecret = environment.getProperty("GOOGLE_CLIENT_SECRET");
        String kakaoClientId = environment.getProperty("KAKAO_CLIENT_ID");
        String kakaoClientSecret = environment.getProperty("KAKAO_CLIENT_SECRET");
        String naverClientId = environment.getProperty("NAVER_CLIENT_ID");
        String naverClientSecret = environment.getProperty("NAVER_CLIENT_SECRET");
        
        log.info("GOOGLE_CLIENT_ID: {}", googleClientId != null && !googleClientId.isEmpty() ? "설정됨 (" + googleClientId.substring(0, Math.min(20, googleClientId.length())) + "...)" : "미설정");
        log.info("GOOGLE_CLIENT_SECRET: {}", googleClientSecret != null && !googleClientSecret.isEmpty() ? "설정됨" : "미설정");
        log.info("KAKAO_CLIENT_ID: {}", kakaoClientId != null && !kakaoClientId.isEmpty() ? "설정됨 (" + kakaoClientId.substring(0, Math.min(20, kakaoClientId.length())) + "...)" : "미설정");
        log.info("KAKAO_CLIENT_SECRET: {}", kakaoClientSecret != null && !kakaoClientSecret.isEmpty() ? "설정됨" : "미설정");
        log.info("NAVER_CLIENT_ID: {}", naverClientId != null && !naverClientId.isEmpty() ? "설정됨 (" + naverClientId + ")" : "미설정");
        log.info("NAVER_CLIENT_SECRET: {}", naverClientSecret != null && !naverClientSecret.isEmpty() ? "설정됨" : "미설정");
        log.info("==========================");
        
        // Google OAuth2 클라이언트
        if (StringUtils.hasText(googleClientId) && StringUtils.hasText(googleClientSecret)) {
            ClientRegistration google = ClientRegistration
                .withRegistrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://pillmate.lion.it.kr/login/oauth2/code/{registrationId}")
                .scope("profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .build();
            registrations.add(google);
            log.info("Google OAuth2 클라이언트가 등록되었습니다.");
            log.info("  - Client ID: {}...", googleClientId.substring(0, Math.min(30, googleClientId.length())));
            log.info("  - 리디렉션 URI: https://pillmate.lion.it.kr/login/oauth2/code/google");
        } else {
            log.warn("Google OAuth2 클라이언트 ID 또는 Secret이 설정되지 않았습니다.");
        }
        
        // Kakao OAuth2 클라이언트
        if (StringUtils.hasText(kakaoClientId) && StringUtils.hasText(kakaoClientSecret)) {
            ClientRegistration kakao = ClientRegistration
                .withRegistrationId("kakao")
                .clientId(kakaoClientId)
                .clientSecret(kakaoClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://pillmate.lion.it.kr/login/oauth2/code/{registrationId}")
                .scope("profile_nickname", "account_email")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .build();
            registrations.add(kakao);
            log.info("Kakao OAuth2 클라이언트가 등록되었습니다.");
            log.info("  - Client ID: {}...", kakaoClientId.substring(0, Math.min(30, kakaoClientId.length())));
            log.info("  - 리디렉션 URI: https://pillmate.lion.it.kr/login/oauth2/code/kakao");
        } else {
            log.warn("Kakao OAuth2 클라이언트 ID 또는 Secret이 설정되지 않았습니다.");
        }
        
        // Naver OAuth2 클라이언트
        if (StringUtils.hasText(naverClientId) && StringUtils.hasText(naverClientSecret)) {
            ClientRegistration naver = ClientRegistration
                .withRegistrationId("naver")
                .clientId(naverClientId)
                .clientSecret(naverClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://pillmate.lion.it.kr/login/oauth2/code/{registrationId}")
                .scope("name", "email")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .userNameAttributeName("response")
                .build();
            registrations.add(naver);
            log.info("Naver OAuth2 클라이언트가 등록되었습니다.");
            log.info("  - Client ID: {}", naverClientId);
            log.info("  - 리디렉션 URI: https://pillmate.lion.it.kr/login/oauth2/code/naver");
        } else {
            log.warn("Naver OAuth2 클라이언트 ID 또는 Secret이 설정되지 않았습니다.");
        }
        
        if (registrations.isEmpty()) {
            log.warn("OAuth2 클라이언트가 하나도 설정되어 있지 않습니다. OAuth2 로그인이 비활성화됩니다.");
            // 빈 리스트를 허용하는 커스텀 리포지토리 반환
            return createEmptyRepository();
        }
        
        log.info("총 {}개의 OAuth2 클라이언트가 등록되었습니다.", registrations.size());
        return new InMemoryClientRegistrationRepository(registrations);
    }
    
    /**
     * 빈 ClientRegistrationRepository를 생성합니다.
     * InMemoryClientRegistrationRepository는 빈 리스트를 허용하지 않으므로,
     * 더미 ClientRegistration을 추가하여 빈 리포지토리를 생성합니다.
     * SecurityConfig에서 실제 등록된 클라이언트만 사용합니다.
     */
    private ClientRegistrationRepository createEmptyRepository() {
        // 빈 리포지토리를 허용하지 않으므로, 사용되지 않는 더미 등록을 추가
        // 실제로는 SecurityConfig에서 findByRegistrationId로 체크하므로 사용되지 않음
        ClientRegistration dummy = ClientRegistration
            .withRegistrationId("dummy")
            .clientId("dummy")
            .clientSecret("dummy")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost/dummy")
            .scope("dummy")
            .authorizationUri("http://localhost/dummy")
            .tokenUri("http://localhost/dummy")
            .userInfoUri("http://localhost/dummy")
            .userNameAttributeName("dummy")
            .build();
        
        List<ClientRegistration> dummyRegistrations = new ArrayList<>();
        dummyRegistrations.add(dummy);
        log.info("더미 OAuth2 리포지토리가 생성되었습니다. (실제 OAuth2 클라이언트가 없음)");
        return new InMemoryClientRegistrationRepository(dummyRegistrations);
    }
}

