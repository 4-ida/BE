package com.pillmate.pillmate.Util;

import java.util.regex.Pattern;

public class PasswordValidator {
    
    // 영문, 숫자, 특수문자를 포함한 8자 이상 패턴
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
    );
    
    /**
     * 비밀번호가 유효한지 검증합니다.
     * - 8자 이상
     * - 영문, 숫자, 특수문자 조합
     * 
     * @param password 검증할 비밀번호
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValid(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}



