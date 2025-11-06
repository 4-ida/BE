package com.pillmate.pillmate.Service;

import org.springframework.stereotype.Component;

@Component
public class TextNormalizer {
    // 소문자 + 공백/기호 제거 + 간단한 단위 제거(예시)
    public String normalize(String s) {
        if (s == null) return "";
        String lower = s.toLowerCase().trim();
        // 공백/기호 제거
        lower = lower.replaceAll("[\\s\\-/_.]", "");
        // 단위/용어 일부 제거(필요시 확장)
        lower = lower.replaceAll("(mg|g|ml|정|캡슐|서방정)", "");
        return lower; //  가공된 lower를 반환
    }
}
