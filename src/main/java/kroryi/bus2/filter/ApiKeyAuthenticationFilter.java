package kroryi.bus2.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.config.security.ApiKeyAuthenticationToken;
import kroryi.bus2.service.apikey.ApiKeyService;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Log4j2
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "API-KEY";
    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * Swagger UI와 관련된 경로 및 기타 허용 경로는 필터 적용 제외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Swagger UI에서 호출된 요청인지 확인
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.startsWith("/favicon")) {
            return true;  // 필터 제외
        }

        // Swagger UI에서 API 호출인 경우만 필터 적용
        String referer = request.getHeader("Referer");
        return referer == null || !referer.contains("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        log.info("ApiKeyAuthenticationFilter------------>");

        // ✅ Swagger UI에서 API 호출인 경우만 필터 작동 (Referer에 swagger-ui 포함된 경우)
        String referer = request.getHeader("Referer");
        boolean isSwaggerRequest = referer != null && referer.contains("/swagger-ui");

        if (!isSwaggerRequest) {
            chain.doFilter(request, response);  // Swagger가 아닌 일반 요청은 필터 통과
            return;
        }

        // ✅ 이미 로그인된 사용자가 ROLE_ADMIN 이면 API 키 없이도 통과
        var currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth != null && currentAuth.isAuthenticated() &&
                currentAuth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            chain.doFilter(request, response);
            return;
        }

        // 🔒 Swagger 요청인데 API 키 없음 → 401
        String apiKey = getApiKeyFromRequest(request);

        // API 키가 없으면 401 Unauthorized 응답
        if (!StringUtils.hasText(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing API Key");
            return;
        }
    }

    private String getApiKeyFromRequest(HttpServletRequest request) {
        return request.getHeader(API_KEY_HEADER);
    }
}
