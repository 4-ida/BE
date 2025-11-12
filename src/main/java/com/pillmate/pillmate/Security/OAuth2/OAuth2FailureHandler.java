package com.pillmate.pillmate.Security.OAuth2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        
        String errorMessage = exception.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = exception.getClass().getSimpleName();
        }
        
        log.error("OAuth2 로그인 실패: {}", errorMessage, exception);
        
        // 에러 타입 확인
        String errorType = exception.getClass().getSimpleName();
        String userFriendlyMessage = "소셜 로그인에 실패했습니다.";
        
        // redirect_uri_mismatch 에러인 경우 더 명확한 메시지 제공
        if (errorMessage.contains("redirect_uri_mismatch") || 
            errorMessage.contains("invalid_request") ||
            request.getQueryString() != null && request.getQueryString().contains("error=redirect_uri_mismatch")) {
            userFriendlyMessage = "리디렉션 URI가 일치하지 않습니다. 개발자 콘솔에서 다음 URI를 등록해야 합니다:\n" +
                "- Google: https://pillmate.lion.it.kr/login/oauth2/code/google\n" +
                "- Kakao: https://pillmate.lion.it.kr/login/oauth2/code/kakao\n" +
                "- Naver: https://pillmate.lion.it.kr/login/oauth2/code/naver";
        } else if (errorMessage.contains("invalid_client")) {
            userFriendlyMessage = "클라이언트 ID 또는 Secret이 잘못되었습니다. 환경 변수를 확인하세요.";
        } else if (errorMessage.contains("access_denied")) {
            userFriendlyMessage = "사용자가 로그인을 취소했습니다.";
        }
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", userFriendlyMessage);
        errorResponse.put("error", errorType);
        errorResponse.put("errorMessage", errorMessage);
        
        // 원인 파악을 위한 추가 정보
        if (exception.getCause() != null) {
            errorResponse.put("cause", exception.getCause().getMessage());
        }
        
        // 디버깅을 위한 요청 정보
        errorResponse.put("requestUri", request.getRequestURI());
        errorResponse.put("queryString", request.getQueryString());
        errorResponse.put("requestUrl", request.getRequestURL().toString());
        errorResponse.put("scheme", request.getScheme());
        errorResponse.put("serverName", request.getServerName());
        errorResponse.put("serverPort", request.getServerPort());
        errorResponse.put("contextPath", request.getContextPath());
        
        // 실제 base URL 계산
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if ((request.getScheme().equals("http") && request.getServerPort() != 80) ||
            (request.getScheme().equals("https") && request.getServerPort() != 443)) {
            baseUrl += ":" + request.getServerPort();
        }
        baseUrl += request.getContextPath();
        errorResponse.put("calculatedBaseUrl", baseUrl);
        
        // 예상되는 리디렉션 URI
        if (request.getRequestURI().contains("/login/oauth2/code/")) {
            String registrationId = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);
            errorResponse.put("expectedRedirectUri", baseUrl + "/login/oauth2/code/" + registrationId);
        }
        
        log.error("OAuth2 실패 상세 정보:");
        log.error("  - Request URI: {}", request.getRequestURI());
        log.error("  - Query String: {}", request.getQueryString());
        log.error("  - Calculated Base URL: {}", baseUrl);
        log.error("  - Error Message: {}", errorMessage);
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}

