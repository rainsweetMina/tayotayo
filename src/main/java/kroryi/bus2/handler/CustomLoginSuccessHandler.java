package kroryi.bus2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.components.RedirectProperties;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.UserService;
import kroryi.bus2.utils.JwtTokenUtil;
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
import java.time.LocalDateTime;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ApplicationContext context;
    private final RedirectProperties redirect;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        /* ------------------------------------------------------------------
         * 1) 로그인 사용자 식별 & 마지막 로그인 시각 저장
         * ------------------------------------------------------------------ */
        String userId = authentication.getName();
        UserService userService = context.getBean(UserService.class);

        userService.updateLastLoginAt(userId);         // ★ DB update
        log.info("📝 lastLoginAt 업데이트 완료: {} -> {}", userId, LocalDateTime.now());

        /* ------------------------------------------------------------------
         * 2) User 재조회 & CustomUserDetails 재생성
         * ------------------------------------------------------------------ */
        User user = userService.findByUserId(userId);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        /* ------------------------------------------------------------------
         * 3) SecurityContext 새로 생성 (세션/토큰 로그인 병행 시 필수)
         * ------------------------------------------------------------------ */
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities());

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(newAuth);
        SecurityContextHolder.setContext(securityContext);

        // 세션에 저장
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext);

        /* ------------------------------------------------------------------
         * 4) JWT 발급
         * ------------------------------------------------------------------ */
        JwtTokenUtil jwtTokenUtil = context.getBean(JwtTokenUtil.class);
        String accessToken  = jwtTokenUtil.generateAccessToken(user);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user);

        /* ------------------------------------------------------------------
         * 5) 리다이렉트 URL 결정
         * ------------------------------------------------------------------ */
        String redirectUrl = redirect.getBaseUrl();   // 기본값(프런트 홈)

        switch (user.getRole().name()) {
            case "ADMIN", "BUS" -> {
                redirectUrl = redirect.getAdminUrl();
                log.info("🔐 {} 권한 → 관리자 대시보드로 이동", user.getRole());
            }
            case "USER" -> {
                SavedRequest saved = new HttpSessionRequestCache().getRequest(request, response);
                redirectUrl = (saved != null)
                        ? saved.getRedirectUrl()
                        : redirect.getUserUrl();
                log.info("👤 USER 리다이렉트: {}", redirectUrl);
            }
        }

        /* ------------------------------------------------------------------
         * 6) 프런트에 JSON 응답
         *    (토큰 + role + 리다이렉트 url)
         * ------------------------------------------------------------------ */
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // 프론트엔드 URL로 이동할 때만 토큰을 파라미터로 추가
        String finalRedirectUrl = redirectUrl;
        if (redirectUrl.contains(":8096")) {
            // 프론트엔드 URL인 경우 토큰 파라미터 추가
            if (redirectUrl.contains("?")) {
                finalRedirectUrl += "&accessToken=" + accessToken + "&refreshToken=" + refreshToken;
            } else {
                finalRedirectUrl += "?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
            }
            log.info("🔗 프론트엔드로 리다이렉트 (토큰 포함): {}", finalRedirectUrl);
        } else {
            // 백엔드 URL인 경우 토큰 파라미터 없이
            log.info("🔗 백엔드로 리다이렉트: {}", finalRedirectUrl);
        }

        String json = String.format("""
            {
              "accessToken" : "%s",
              "refreshToken": "%s",
              "role"        : "%s",
              "redirect"    : "%s"
            }""",
<<<<<<< Updated upstream
                accessToken, refreshToken, user.getRole().name(), redirectUrlWithTokens);
=======
                accessToken, refreshToken, user.getRole().name(), finalRedirectUrl);
>>>>>>> Stashed changes

        response.getWriter().write(json);
    }
}
