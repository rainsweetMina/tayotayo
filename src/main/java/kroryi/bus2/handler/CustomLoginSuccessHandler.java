package kroryi.bus2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.entity.user.User;
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
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ApplicationContext context;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // ✅ 세션에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String userId = authentication.getName();

        // 순환 참조 방지: 지연 주입
        UserService userService = context.getBean(UserService.class);

        // 로그인 시간 갱신
        userService.updateLastLoginAt(userId);

        // 사용자 정보 조회
        User user = userService.findByUserId(userId);

        // JSON 응답 설정
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        // JSON 문자열로 사용자 정보 응답
        String json = String.format(
                "{\"message\": \"success\", \"userId\": \"%s\", \"role\": \"%s\"}",
                user.getUserId(), user.getRole().name()
        );

        response.getWriter().write(json);
        log.info("✅ 로그인 성공 (JSON): userId={}, role={}", user.getUserId(), user.getRole());
    }
}
