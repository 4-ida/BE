package com.pillmate.pillmate.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 개발용)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 사용으로 세션 비활성화
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll() // 인증 API 허용 (기존 경로)
                .requestMatchers("/api/v1/signup", "/api/v1/login").permitAll() // 회원가입, 로그인 API 허용
                .requestMatchers("/api/dose-events/**").permitAll() // DoseEvent API 허용 (개발용)
                .requestMatchers("/swagger.html", "/swagger-ui/**", "/api-docs/**").permitAll() // Swagger 허용
                .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 허용 (개발용)
                .anyRequest().authenticated() // 나머지는 인증 필요
            )
            .formLogin(form -> form.disable()) // 기본 로그인 폼 비활성화
            .httpBasic(basic -> basic.disable()); // HTTP Basic 인증 비활성화
        
        return http.build();
    }
}
