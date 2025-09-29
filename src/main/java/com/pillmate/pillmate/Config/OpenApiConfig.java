package com.pillmate.pillmate.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PillMate API")
                        .description("PillMate 애플리케이션의 REST API 문서")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("PillMate Team")
                                .email("pillmate@example.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 토큰을 입력하세요. 로그인 API에서 받은 토큰을 사용하세요.")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
