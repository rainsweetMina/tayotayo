package kroryi.bus2.service.swagger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class SwaggerWriteBlockInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String referer = request.getHeader("Referer");

        if (referer != null && referer.contains("/swagger-ui")) {
            String method = request.getMethod();

            // 관리자라면 차단하지 않음
            if (isAdmin()) {
                return true;
            }

            // 관리자 아니고 쓰기 요청이면 차단
            if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method) || "PATCH".equals(method)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Swagger에서는 쓰기 요청이 차단됩니다.");
                return false;
            }
        }

        return true;
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() &&
                auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
