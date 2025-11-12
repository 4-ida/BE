package com.pillmate.pillmate.Util;

import java.util.regex.Pattern;

public class EmailValidator {
    
    // 이메일 형식 검증 패턴
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    /**
     * 이메일 형식이 유효한지 검증합니다.
     * 
     * @param email 검증할 이메일
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 이메일을 정규화합니다 (trim, 소문자 변환).
     * 
     * @param email 정규화할 이메일
     * @return 정규화된 이메일 (null이면 null 반환)
     */
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}

