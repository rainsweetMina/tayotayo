package kroryi.bus2.config.web;

import kroryi.bus2.service.swagger.SwaggerWriteBlockInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.found-location}")
    private String filePath;

    private final SwaggerWriteBlockInterceptor interceptor;

    public WebConfig(SwaggerWriteBlockInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // ✅ 공통 API CORS
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "https://docs.yi.or.kr:8094",
                        "https://192.168.10.47:8094",
                        "https://localhost:8094",
                        "http://localhost:5173",  // Vue.js 개발 서버 (HTTP)
                        "https://localhost:5173", // Vue.js 개발 서버 (HTTPS)
                        "http://localhost:5174",
                        "https://localhost:5174",
                        "http://192.168.10.13:5173",
                        "http://192.168.10.13:5174"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600)
                .allowCredentials(true);

        // ✅ Swagger 관련 CORS
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins("https://docs.yi.or.kr:8094", "https://192.168.10.47:8094","https://localhost:8094")
                .allowedMethods("*");

        registry.addMapping("/swagger-ui/**") // 만약 Swagger UI 경로도 CORS 걸리면 추가
                .allowedOrigins("https://docs.yi.or.kr:8094", "https://192.168.10.47:8094","https://localhost:8094")
                .allowedMethods("*");

        registry.addMapping("/api/**")  // ✅ 추가
                .allowedOrigins("https://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/**"); // 감시할 경로 지정
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Vue 라우터에서 사용하는 경로를 index.html로 포워딩
        registry.addViewController("/login/oauth2/success").setViewName("forward:/index.html");
        registry.addViewController("/admin").setViewName("forward:/index.html");
        registry.addViewController("/admin/**").setViewName("forward:/index.html");
        registry.addViewController("/bus").setViewName("forward:/index.html");
        registry.addViewController("/bus/**").setViewName("forward:/index.html");
        registry.addViewController("/mypage").setViewName("forward:/index.html");
        registry.addViewController("/mypage/**").setViewName("forward:/index.html");

        registry.addViewController("/login").setViewName("forward:/index.html");

        // 기본 홈 경로도 index.html로
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}

