package kroryi.bus2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.components.RedirectProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private final RedirectProperties redirect;

    public CustomLogoutSuccessHandler(RedirectProperties redirect) {
        this.redirect = redirect;
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        log.info("✅ 로그아웃 성공");

        String referer = request.getHeader("Referer");   // 직전에 보이던 주소
        if (referer != null && !referer.contains("/auth/login")) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(redirect.getBaseUrl()+ "/main"); // fallback
        }
    }
}
