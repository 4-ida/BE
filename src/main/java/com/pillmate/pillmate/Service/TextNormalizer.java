package com.pillmate.pillmate.Service;

public class TextNormalizer {
    // 소문자 + 단위/기호 제거(간단 버전)
    public static String normalize(String s) {
        if (s == null) return "";
        String lower = s.toLowerCase();
        // 흔한 단위/기호 제거(필요시 추가)
        lower = lower.replaceAll("[\\s\\-/_.]", "");
        lower = lower.replaceAll("(mg|g|ml|정|캡슐|서방정)", "");
        return lower;
    }
}
