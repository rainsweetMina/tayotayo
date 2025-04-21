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

        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins("https://localhost:8081")
                .allowedMethods("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/uploads/";

        registry.addResourceHandler("/files/**") // ← 브라우저가 접근하는 경로
                .addResourceLocations("file:///" + uploadPath); // ← 실제 저장 폴더

        registry.addResourceHandler("/uploads/found/**")
                .addResourceLocations("file:"+ filePath);
        // ✅ 광고 이미지 접근 경로 추가됨
        registry.addResourceHandler("/uploads/ad/**")
                .addResourceLocations("file:" + uploadPath + "ad/");
    }


    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/uploads/found/**")
//                .addResourceLocations("file:"+ filePath);
//    }


}

