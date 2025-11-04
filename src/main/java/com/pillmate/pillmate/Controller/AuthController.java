package com.pillmate.pillmate.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pillmate.pillmate.Config.JwtUtil;
import com.pillmate.pillmate.DTO.ConsentDto;
import com.pillmate.pillmate.DTO.LoginRequest;
import com.pillmate.pillmate.DTO.SignUpRequest;
import com.pillmate.pillmate.DTO.SignUpResponse;
import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "사용자 인증 관련 API")
public class AuthController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복, 비밀번호 불일치 등)")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            User user = userService.signUp(request);
            
            // JWT 토큰 생성
            String token = jwtUtil.generateToken(user.getEmail());
            
            // 약관 동의 정보
            ConsentDto consent = ConsentDto.builder()
                    .termsOfService(user.getTermsOfService())
                    .privacyPolicy(user.getPrivacyPolicy())
                    .dataUsage(user.getDataUsage())
                    .build();
            
            // 사용자 정보
            SignUpResponse.UserInfo userInfo = SignUpResponse.UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();
            
            // Response 데이터 구성
            SignUpResponse.SignUpData data = SignUpResponse.SignUpData.builder()
                    .user(userInfo)
                    .consent(consent)
                    .token(token)
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
            
            // Response 생성
            SignUpResponse response = SignUpResponse.builder()
                    .message("회원가입 성공")
                    .data(data)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @Operation(summary = "로그인", description = "사용자 로그인을 수행하고 JWT 토큰을 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            
            // JWT 토큰 생성
            String token = jwtUtil.generateToken(user.getEmail());
            
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
