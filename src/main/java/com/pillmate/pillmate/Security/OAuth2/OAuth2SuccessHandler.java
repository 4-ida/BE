package com.pillmate.pillmate.Security.OAuth2;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pillmate.pillmate.Config.JwtUtil;
import com.pillmate.pillmate.DTO.LoginResponse;
import com.pillmate.pillmate.Security.CustomUserDetails;
import com.pillmate.pillmate.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean firstLogin = userService.markLogin(userDetails.getId());

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(userDetails.getEmail());
        
        // 사용자 정보
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .name(userDetails.getUser().getName())
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
        
        // 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
    }
}

