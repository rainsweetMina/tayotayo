package kroryi.bus2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ApplicationContext context;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String userId = authentication.getName();

        // 🔁 순환 참조 방지: 여기서 지연 주입
        UserService userService = context.getBean(UserService.class);
        User user = userService.findByUserId(userId);

        if (user != null && user.getRole() == Role.ADMIN) {
            log.info("✅ 관리자 로그인 성공: {}", userId);
            response.sendRedirect("/admin");
        } else {
            log.info("✅ 일반 사용자 로그인 성공: {}", userId);
            response.sendRedirect("/mypage");
        }
    }
}
