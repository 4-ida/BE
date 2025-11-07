package com.pillmate.pillmate.Util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
        // if (principal instanceof CustomUserDetails cud) return cud.getId();

        // 2) 기본: name을 Long으로 파싱
        String name;
        if (principal instanceof UserDetails ud) name = ud.getUsername();
        else name = auth.getName();

        try {
            return Long.parseLong(name);
        } catch (NumberFormatException e) {
            // 팀 규약에 맞게 변경 필요할 수 있음
            throw new IllegalStateException("Cannot resolve userId from principal name=" + name);
        }
    }
}
