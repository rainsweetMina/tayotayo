package kroryi.bus2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {

//        // XML 메시지 컨버터 추가
//        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
//        messageConverters.add(new MappingJackson2XmlHttpMessageConverter(new XmlMapper()));
//        restTemplate.setMessageConverters(messageConverters);

        return new RestTemplate();
    }
}