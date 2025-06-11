package kroryi.bus2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final ApplicationContext context;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String userId = authentication.getName();
        log.info("✅ 소셜 로그인 성공 - userId: {}", userId);

        // 마지막 로그인 시간 업데이트
        UserService userService = context.getBean(UserService.class);
        userService.updateLastLoginAt(userId);

        // ✅ 프론트 마이페이지로 강제 리다이렉트
        response.sendRedirect("https://localhost:5173/mypage");
    }
}
