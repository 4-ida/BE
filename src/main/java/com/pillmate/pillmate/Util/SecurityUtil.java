package com.pillmate.pillmate.Util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.pillmate.pillmate.Security.CustomUserDetails;

/**
 * JWT 인증 기반에서 현재 로그인 사용자의 식별자(userId)를 얻는 유틸.
 * - 기본 구현: principal의 name을 Long으로 파싱
 * - 팀 프로젝트의 CustomUserDetails에 getId()가 있다면 거기에 맞춰 수정
 */
public final class SecurityUtil {
    private SecurityUtil() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = auth.getPrincipal();

        // 1) 커스텀 UserDetails에 getId()가 있는 경우 (예시)
        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }

        // 2) 기타 타입은 지원하지 않음
        throw new IllegalStateException("Cannot resolve userId from principal type=" + principal.getClass().getName());
    }
}
