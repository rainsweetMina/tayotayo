package kroryi.bus2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
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

        UserService userService = context.getBean(UserService.class);
        userService.updateLastLoginAt(userId);

        // ✅ 사용자 정보 재조회 → CustomUserDetails 생성
        User user = userService.findByUserId(userId);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // ✅ 인증 객체 재생성
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
                );

        // ✅ SecurityContext 생성 및 저장
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(newAuth);
        SecurityContextHolder.setContext(securityContext);

        // ✅ 세션에 SecurityContext 저장
        request.getSession(true)
                .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        // ✅ 리다이렉트 URL 결정
        String redirectUrl = "https://localhost:5173/";

        switch (user.getRole().name()) {
            case "ADMIN" -> redirectUrl = "/admin/dashboard";
            case "BUS" -> redirectUrl = "/bus";
            case "USER" -> {
                SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
                if (savedRequest != null) {
                    redirectUrl = savedRequest.getRedirectUrl();
                    log.info("👤 USER - 이전 요청 페이지로 리다이렉트: {}", redirectUrl);
                } else {
                    redirectUrl = "/mypage";
                    log.info("👤 USER - 기본 마이페이지로 이동");
                }
            }
        }

        log.info("🔑 로그인 성공: {} → {}", userId, redirectUrl);
        response.sendRedirect("https://localhost:5173" + redirectUrl);
    }
}
