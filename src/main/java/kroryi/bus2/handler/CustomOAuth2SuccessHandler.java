package kroryi.bus2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.config.security.CustomOAuth2User;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Log4j2
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final ApplicationContext context;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String userId = oAuth2User.getUserId();

        UserService userService = context.getBean(UserService.class);
        userService.updateLastLoginAt(userId);
        User user = userService.findByUserId(userId);


        String role = user.getRole().name();
        String path = switch (role) {
            case "ADMIN" -> "/admin/dashboard";
            case "BUS" -> "/bus";
            default -> "/mypage";
        };

        // ✅ 프론트에서 redirect 쿼리 파라미터를 읽어 처리할 수 있게 보냄
        String redirectUrl = "https://localhost:5173" + path;

        log.info("✅ 소셜 로그인 성공! userId={}, role={}, redirect={}", userId, role, redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
