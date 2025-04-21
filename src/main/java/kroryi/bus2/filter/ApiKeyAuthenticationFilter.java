package kroryi.bus2.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.config.security.ApiKeyAuthenticationToken;
import kroryi.bus2.service.apikey.ApiKeyService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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

        // Swagger UI 관련 경로는 필터 제외
//        return path.startsWith("/swagger-ui")
//                || path.startsWith("/v3/api-docs")
//                || path.startsWith("/swagger-resources")
//                || path.startsWith("/webjars")
//                || path.startsWith("/csrf")
//                || path.startsWith("/error")
//                || path.startsWith("/login")  // 로그인 경로도 제외
//                || path.startsWith("/register");  // 회원가입 경로도 제외
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String apiKey = getApiKeyFromRequest(request);

        // API 키가 없으면 401 Unauthorized 응답
        if (!StringUtils.hasText(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing API Key");
            return;
        }

        // API 키가 유효하지 않으면 401 Unauthorized 응답
        if (!apiKeyService.isValidApiKey(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return;
        }

        // 권한 설정: 기본은 ROLE_USER, 관리자는 ROLE_ADMIN
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        if (apiKeyService.isAdminApiKey(apiKey)) {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        // 인증 객체 생성 후 SecurityContext에 설정
        ApiKeyAuthenticationToken authToken = new ApiKeyAuthenticationToken(apiKey, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 필터 체인 진행
        chain.doFilter(request, response);
    }

    private String getApiKeyFromRequest(HttpServletRequest request) {
        return request.getHeader(API_KEY_HEADER);
    }
}
