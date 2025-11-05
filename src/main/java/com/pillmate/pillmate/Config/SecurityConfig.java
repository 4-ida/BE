package com.pillmate.pillmate.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.pillmate.pillmate.Security.OAuth2.OAuth2FailureHandler;
import com.pillmate.pillmate.Security.OAuth2.OAuth2SuccessHandler;
import com.pillmate.pillmate.Security.OAuth2.OAuth2UserProviderRouter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final OAuth2UserProviderRouter oAuth2UserProviderRouter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 개발용)
            // OAuth2 로그인을 위해 세션을 사용하되, OAuth2 인증 후에는 STATELESS로 처리
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll() // 인증 API 허용 (기존 경로)
                .requestMatchers("/api/v1/signup", "/api/v1/auth/login").permitAll() // 회원가입, 로그인 API 허용
                .requestMatchers("/api/v1/main/calendar/**").permitAll() // 캘린더 일정 API 허용 (개발용)
                .requestMatchers("/oauth2/**", "/login/**").permitAll() // OAuth2 소셜 로그인 관련 요청 허용
                .requestMatchers("/swagger.html", "/swagger-ui/**", "/api-docs/**").permitAll() // Swagger 허용
                .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 허용 (개발용)
                .anyRequest().authenticated() // 나머지는 인증 필요
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserProviderRouter) // 사용자 정보 받아오기
                )
                .successHandler(oAuth2SuccessHandler) // JWT 발급 및 응답
                .failureHandler(oAuth2FailureHandler) // 오류 처리
            )
            .formLogin(form -> form.disable()) // 기본 로그인 폼 비활성화
            .httpBasic(basic -> basic.disable()); // HTTP Basic 인증 비활성화
        
        return http.build();
    }
}
