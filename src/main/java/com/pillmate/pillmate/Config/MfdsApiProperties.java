package com.pillmate.pillmate.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "external.mfds")
public class MfdsApiProperties {
    /**
     * 기본 API URL (예: https://apis.data.go.kr/1471000)
     */
    private String baseUrl;

    /**
     * 식약처 OpenAPI 서비스 키.
     * 반드시 운영 환경에서 환경 변수 MFDS_SERVICE_KEY 로 주입하세요.
     */
    private String serviceKey;
}

