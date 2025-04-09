package kroryi.bus2.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
// api 호출용 링크인거같은데 지금은 안쓰는듯함
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://apis.data.go.kr/6270000/dbmsapi01")
                .build();
    }
}
