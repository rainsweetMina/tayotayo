package kroryi.bus2.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.found-location}")
    private String filePath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/ds/api/**")
                .allowedOrigins("https://localhost:8081")
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                .allowCredentials(true);
    }
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/found/**")
                .addResourceLocations("file:"+ filePath);
    }
}

