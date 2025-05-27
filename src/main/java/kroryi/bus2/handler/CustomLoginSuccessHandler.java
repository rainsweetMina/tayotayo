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
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

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

        // ✅ 최근 로그인 시간 갱신 (엔티티 직접 수정 X)
        userService.updateLastLoginAt(userId);

        // ✅ 세션에 저장된 redirect 우선 처리
        String sessionRedirect = (String) request.getSession().getAttribute("redirectAfterLogin");
        if (sessionRedirect != null && !sessionRedirect.isBlank()) {
            request.getSession().removeAttribute("redirectAfterLogin");
            log.info("🔁 세션 저장 리다이렉트: {}", sessionRedirect);
            response.sendRedirect(sessionRedirect);
            return;
        }

        // ✅ 사용자 다시 조회 (선택)
        User user = userService.findByUserId(userId); // 권한 분기 위해 재조회

        // ✅ 기본 리다이렉트
        if (user != null && user.getRole() == Role.ADMIN) {
            log.info("✅ 관리자 로그인 성공: {}", userId);
            response.sendRedirect("/admin/dashboard");
        } else {
            log.info("✅ 일반 사용자 로그인 성공: {}", userId);
            response.sendRedirect("/mypage");
        }
    }


}
