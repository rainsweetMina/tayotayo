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

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String userId = authentication.getName();

        UserService userService = context.getBean(UserService.class);
        userService.updateLastLoginAt(userId);
        User user = userService.findByUserId(userId);

        // 리다이렉트 경로 설정
        String redirectUrl = "/mypage";
        if (user.getRole().name().equals("ADMIN")) {
            redirectUrl = "/admin/dashboard";
        }

        // 리다이렉트 수행
        response.sendRedirect(redirectUrl);
    }

}
