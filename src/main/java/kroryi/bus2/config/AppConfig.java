package kroryi.bus2.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// 외부 API 호출을 위한 RestTemplate의 Bean을 생성하여 Spring 컨테이너에 등록합니다.
public class AppConfig {

    // 해당 메서드를 통해 RestTemplate을 Bean으로 등록하면, @Autowired 또는 @RequiredArgsConstructor 등을 통해 어디서든 주입받아 사용가능
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("Bus")
                .pathsToMatch("/api/bus/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adGroup() {
        return GroupedOpenApi.builder()
                .group("Ad")
                .packagesToScan("kroryi.bus2.controller.ad")
                .build();
    }

    @Bean
    public GroupedOpenApi boardGroup(){
        return GroupedOpenApi.builder()
                .group("Board")
                .packagesToScan("kroryi.bus2.controller.board")
                .build();
    }

    @Bean
    public GroupedOpenApi busGroup(){
        return GroupedOpenApi.builder()
                .group("Bus")
                .packagesToScan("kroryi.bus2.controller.bus")
                .build();
    }

    @Bean
    public GroupedOpenApi noticeGroup(){
        return GroupedOpenApi.builder()
                .group("Notice")
                .packagesToScan("kroryi.bus2.controller.admin.notice")
                .build();
    }

    @Bean
    public GroupedOpenApi monitoringGroup(){
        return GroupedOpenApi.builder()
                .group("Monitoring")
                .packagesToScan("kroryi.bus2.controller.admin.monitoring")
                .build();
    }

    @Bean
    public GroupedOpenApi mypageGroup(){
        return GroupedOpenApi.builder()
                .group("MyPage")
                .packagesToScan("kroryi.bus2.controller.mypage")
                .build();
    }

    @Bean
    public GroupedOpenApi lostGroup() {
        return GroupedOpenApi.builder()
                .group("LostFound")
                .packagesToScan("kroryi.bus2.controller.lost")
                .build();
    }

    @Bean
    public GroupedOpenApi qnaGroup() {
        return GroupedOpenApi.builder()
                .group("Qna")
                .packagesToScan("kroryi.bus2.controller.qna")
                .build();
    }

    @Bean
    public GroupedOpenApi userGroup(){
        return GroupedOpenApi.builder()
                .group("User")
                .pathsToMatch("/api/user/**")
                .build();
    }

    // Swagger UI에서 API 경로 설정
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("All")  // API 버전 설정
                .pathsToMatch("/api/**")  // /api/로 시작하는 경로에 Swagger UI 적용
                .build();
    }
}