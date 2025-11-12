package com.pillmate.pillmate.Security;

import com.pillmate.pillmate.Config.JwtUtil;
import com.pillmate.pillmate.Repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 회원가입, 로그인, Swagger 등 인증 불필요한 경로는 필터 스킵
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/v1/signup") || 
            requestPath.startsWith("/api/v1/auth/login") ||
            requestPath.startsWith("/api/auth/") ||
            requestPath.startsWith("/oauth2/") ||
            requestPath.startsWith("/login/") ||
            requestPath.startsWith("/swagger-ui") ||
            requestPath.startsWith("/v3/api-docs") ||
            requestPath.startsWith("/swagger-resources") ||
            requestPath.startsWith("/webjars") ||
            requestPath.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = authHeader.substring(7);
        
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.getEmailFromToken(token);
            
            userRepository.findByEmail(email).ifPresent(user -> {
                CustomUserDetails userDetails = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        
        filterChain.doFilter(request, response);
    }
}


