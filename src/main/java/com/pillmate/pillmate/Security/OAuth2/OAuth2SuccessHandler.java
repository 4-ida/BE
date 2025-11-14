package com.pillmate.pillmate.Security.OAuth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pillmate.pillmate.Config.JwtUtil;
import com.pillmate.pillmate.DTO.LoginResponse;
import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Security.CustomUserDetails;
import com.pillmate.pillmate.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final Environment environment;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean firstLogin = userService.markLogin(userDetails.getId());

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(userDetails.getEmail());
        
        // 사용자 정보
        User user = userDetails.getUser();
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .name(user.getName())
                .build();
        
        // Response 생성
        LoginResponse loginResponse = LoginResponse.builder()
                .message("로그인 성공")
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresInMillis(jwtUtil.getExpirationTimeMillis())
                .user(userInfo)
                .firstLogin(firstLogin)
                .build();
        
        // 프론트엔드 URL 확인 (쿼리 파라미터 또는 환경 변수)
        String redirectUri = request.getParameter("redirect_uri");
        if (!StringUtils.hasText(redirectUri)) {
            // 환경 변수에서 프론트엔드 URL 가져오기
            redirectUri = environment.getProperty("FRONTEND_URL", "https://pillmate-three.vercel.app");
        }
        
        // 허용된 프론트엔드 URL 목록
        String[] allowedFrontendUrls = {
            "https://pillmate-three.vercel.app",
            "https://pillmate.lion.it.kr",
            "http://localhost:3000",
            "http://localhost:5173"
        };
        
        // redirectUri가 허용된 URL인지 확인
        boolean isAllowed = false;
        for (String allowedUrl : allowedFrontendUrls) {
            if (redirectUri.startsWith(allowedUrl)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            // 허용되지 않은 URL인 경우 기본값 사용
            log.warn("허용되지 않은 프론트엔드 URL: {}. 기본값 사용: https://pillmate-three.vercel.app", redirectUri);
            redirectUri = "https://pillmate-three.vercel.app";
        }
        
        // JSON 응답을 URL 인코딩하여 쿼리 파라미터로 전달
        String jsonResponse = objectMapper.writeValueAsString(loginResponse);
        String encodedResponse = URLEncoder.encode(jsonResponse, StandardCharsets.UTF_8);
        
        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String redirectUrl = redirectUri + "/oauth2/callback?data=" + encodedResponse;
        
        log.info("OAuth2 로그인 성공 - 프론트엔드로 리다이렉트: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}

