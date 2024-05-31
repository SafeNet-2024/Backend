package com.SafeNet.Backend.global.config;


import com.google.common.net.HttpHeaders;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "GroShare API",
                description = "그로셰어 웹 서비스 API 명세서",
                version = "v1"))
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("v1-definition")
                .pathsToMatch("/api/**")
                .build();
    }
    @Bean
    public OpenAPI openAPI() {
        String key = "ACCESS_TOKEN";
        String refreshKey = "REFRESH_TOKEN";

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(key)
                .addList(refreshKey);

        SecurityScheme accessTokenSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)         // HTTP 인증 방식을 지정
                .scheme("bearer")                       // Bearer 토큰 방식을 지정
                .bearerFormat("JWT")                    // JWT 형식을 지정
                .in(SecurityScheme.In.HEADER)           // HTTP 헤더에 포함됨을 지정
                .name(key);

        SecurityScheme refreshTokenSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)         // HTTP 인증 방식을 지정
                .in(SecurityScheme.In.HEADER)           // HTTP 헤더에 포함됨을 지정
                .name(refreshKey);

        Components components = new Components()
                .addSecuritySchemes(key, accessTokenSecurityScheme)
                .addSecuritySchemes(refreshKey, refreshTokenSecurityScheme);

        return new OpenAPI()
                .components(new Components())
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}