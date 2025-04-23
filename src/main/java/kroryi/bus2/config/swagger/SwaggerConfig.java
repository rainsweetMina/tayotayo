package kroryi.bus2.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "ApiKeyAuth",  // API 키 인증 방식 이름
        type = SecuritySchemeType.APIKEY,  // API 키 인증 방식을 선택
        in = SecuritySchemeIn.HEADER,  // 인증을 위한 API 키를 HTTP 헤더에서 받음
        paramName = "API-KEY"  // 요청 헤더에 포함될 API 키의 이름
)
public class SwaggerConfig {

    // OpenAPI 설정
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("ApiKeyAuth",  // 인증 방식 추가
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                                        .name("API-KEY")  // API-KEY 헤더 이름
                        ))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("ApiKeyAuth"))  // SecurityRequirement 추가
                .info(new Info()
                        .title("버스2 API")
                        .version("1.0.0")
                        .description("API 키 인증 기반 공공데이터 API 문서입니다."));
    }

    // Swagger에서 API 경로 정의
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("all")  // API 버전 설정
                .pathsToMatch("/api/**")  // /api/로 시작하는 모든 경로를 포함
                .build();
    }
}
