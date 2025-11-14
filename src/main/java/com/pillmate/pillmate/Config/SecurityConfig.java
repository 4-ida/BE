package com.pillmate.pillmate.Config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.pillmate.pillmate.Security.JwtAuthenticationFilter;
import com.pillmate.pillmate.Security.OAuth2.OAuth2FailureHandler;
import com.pillmate.pillmate.Security.OAuth2.OAuth2SuccessHandler;
import com.pillmate.pillmate.Security.OAuth2.OAuth2UserProviderRouter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final OAuth2UserProviderRouter oAuth2UserProviderRouter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ClientRegistrationRepository clientRegistrationRepository;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // OAuth2 클라이언트가 설정되어 있는지 확인
        boolean oauth2Enabled = clientRegistrationRepository.findByRegistrationId("google") != null ||
                               clientRegistrationRepository.findByRegistrationId("kakao") != null ||
                               clientRegistrationRepository.findByRegistrationId("naver") != null;
        
        log.info("OAuth2 설정 상태:");
        if (oauth2Enabled) {
            log.info("  Google: {}", clientRegistrationRepository.findByRegistrationId("google") != null ? "활성화" : "비활성화");
            log.info("  Kakao: {}", clientRegistrationRepository.findByRegistrationId("kakao") != null ? "활성화" : "비활성화");
            log.info("  Naver: {}", clientRegistrationRepository.findByRegistrationId("naver") != null ? "활성화" : "비활성화");
        } else {
            log.info("  OAuth2 클라이언트가 설정되어 있지 않습니다.");
        }
        log.info("  OAuth2 활성화: {}", oauth2Enabled);
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 개발용)
            // OAuth2 로그인을 위해 세션을 사용하되, OAuth2 인증 후에는 STATELESS로 처리
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll() // 인증 API 허용 (기존 경로)
                .requestMatchers("/api/v1/signup", "/api/v1/signup/**").permitAll() // 회원가입 및 이메일 중복 확인 허용
                .requestMatchers("/api/v1/auth/login").permitAll() // 로그인 허용
                .requestMatchers("/oauth2/**", "/login/**").permitAll() // OAuth2 소셜 로그인 관련 요청 허용
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll() // Swagger 허용 (개발/프로덕션 모두)
                .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 허용 (개발용)
                .anyRequest().authenticated() // 나머지는 인증 필요 (캘린더 일정 API 포함)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"접근 권한이 없습니다.\"}");
                })
            );
        
        // OAuth2 클라이언트가 설정되어 있을 때만 OAuth2 로그인 활성화
        if (oauth2Enabled) {
            log.info("OAuth2 로그인이 활성화됩니다.");
            http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserProviderRouter) // 사용자 정보 받아오기
                )
                .successHandler(oAuth2SuccessHandler) // JWT 발급 및 응답
                .failureHandler(oAuth2FailureHandler) // 오류 처리
            );
        } else {
            log.info("OAuth2 클라이언트 ID가 설정되어 있지 않아 OAuth2 로그인이 비활성화됩니다.");
        }
        
        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT 필터 추가
            .formLogin(form -> form.disable()) // 기본 로그인 폼 비활성화
            .httpBasic(basic -> basic.disable()); // HTTP Basic 인증 비활성화
        
        return http.build();
    }
    
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 개발 및 프로덕션 환경 Origin 허용
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000", 
            "http://localhost:5173",  // Vite 개발 서버
            "http://localhost:8080", 
            "http://127.0.0.1:8080", 
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173",  // Vite 개발 서버
            "https://pillmate.lion.it.kr", // 프로덕션 도메인
            "https://pillmate-three.vercel.app" // Vercel 배포 도메인
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization")); // JWT 토큰을 위한 헤더 노출
        configuration.setMaxAge(3600L); // Preflight 요청 캐시 시간
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/webjars/**",
        "/h2-console/**"
    );
    }
}    
