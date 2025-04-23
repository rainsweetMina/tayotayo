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
public class ApiKeyAuthenticationFilter{
//public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

//    private final ApiKeyService apiKeyService;
//    private static final String API_KEY_HEADER = "API-KEY";
//
//    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
//        this.apiKeyService = apiKeyService;
//        System.out.println(">>> ApiKeyAuthenticationFilter 생성됨 <<<");
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getRequestURI();
//        System.out.println("🔥 ApiKey 필터 체크 중: " + path);
//
//        String[] excludedPaths = {
//                "/swagger-ui", "/v3/api-docs", "/swagger-resources", "/webjars",
//                "/csrf", "/error", "/login", "/logout", "/register", "/bus", "/schedule",
//                "/admin", "/mypage"
//        };
//
//        for (String excluded : excludedPaths) {
//            if (path.startsWith(excluded)) {
//                System.out.println("❌ 필터 제외 경로: " + path);
//                return true;
//            }
//        }
//
//        // 필터 타도록
//        System.out.println("✅ 필터 적용됨: " + path);
//        return false;
//    }
//
//
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
//        String apiKey = request.getHeader(API_KEY_HEADER);
//        if (!StringUtils.hasText(apiKey)) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"error\":\"Unauthorized: API Key missing\"}");
//            return;
//        }
//
//        if (!apiKeyService.isValidApiKey(apiKey)) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"error\":\"Unauthorized: Invalid API Key\"}");
//            return;
//        }
//
//        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
//        if (apiKeyService.isAdminApiKey(apiKey)) {
//            authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
//        }
//
//        ApiKeyAuthenticationToken authToken = new ApiKeyAuthenticationToken(apiKey, authorities);
//        authToken.setAuthenticated(true);
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        chain.doFilter(request, response);
//    }
}
