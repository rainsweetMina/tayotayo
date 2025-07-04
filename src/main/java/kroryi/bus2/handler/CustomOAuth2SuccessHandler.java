package kroryi.bus2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.components.RedirectProperties;
import kroryi.bus2.config.security.CustomOAuth2User;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Log4j2
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final ApplicationContext context;
    private final RedirectProperties redirect;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("✅ OAuth2 로그인 성공 처리 시작");

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String userId = oAuth2User.getUserId();

        // ⛳ UserService 가져오기 (지연 로딩용)
        UserService userService = context.getBean(UserService.class);
        userService.updateLastLoginAt(userId);

        // ⛳ 유저 조회 및 UserDetails 생성
        User user = userService.findByUserId(userId);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // ⛳ 인증 객체 재생성
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
                );

        // ⛳ SecurityContext 수동 설정
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(newAuth);
        SecurityContextHolder.setContext(securityContext);

        // ✅ 세션에 SecurityContext 저장
        request.getSession(true)
                .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        // JWT 토큰 생성
        JwtTokenUtil jwtTokenUtil = context.getBean(JwtTokenUtil.class);
        String accessToken = jwtTokenUtil.generateAccessToken(user);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user);

        // ⛳ 리다이렉트 경로 결정
        String role = user.getRole().name();
        String path = switch (role) {
            case "ADMIN" -> "/admin/dashboard";
            case "BUS" -> "/bus";
            default -> "/oauth-success"; // 중간 페이지
        };

        // JWT 토큰을 URL 파라미터로 전달
        String encodedAccessToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        String encodedRefreshToken = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
        
        String redirectUrl = String.format(redirect.getBaseUrl() + "%s?accessToken=%s&refreshToken=%s",
                path, encodedAccessToken, encodedRefreshToken);
        
        log.info("✅ 리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
