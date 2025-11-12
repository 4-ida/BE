package com.pillmate.pillmate.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * OAuth2 리디렉션 URI 설정
 * 리버스 프록시(nginx)를 통해 들어오는 요청에서 올바른 baseUrl을 사용하도록 설정
 */
@Configuration
public class OAuth2RedirectUriConfig {
    
    /**
     * ForwardedHeaderFilter를 등록하여 X-Forwarded-* 헤더를 처리합니다.
     * 리버스 프록시(nginx)를 통해 들어오는 요청에서 올바른 scheme, host, port를 인식하도록 합니다.
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}

