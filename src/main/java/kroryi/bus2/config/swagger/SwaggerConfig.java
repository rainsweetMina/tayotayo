package kroryi.bus2.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        security = {
                @SecurityRequirement(name = "ApiKeyAuth")  // 전체 API에서 API-KEY 인증을 요구
        }
)
@SecurityScheme(
        name = "ApiKeyAuth",  // 인증 이름
        type = SecuritySchemeType.APIKEY,  // 인증 타입을 API 키로 설정
        in = SecuritySchemeIn.HEADER,  // 요청 헤더에 API-KEY를 포함
        paramName = "API-KEY"  // 헤더 이름 설정
)
public class SwaggerConfig {

    // OpenAPI 설정
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("버스2 API")
                        .version("1.0.0")
                        .description("API 키 인증 기반 공공데이터 API 문서입니다."))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("ApiKeyAuth"));
    }


}
