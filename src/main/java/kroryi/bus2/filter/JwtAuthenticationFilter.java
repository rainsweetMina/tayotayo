package kroryi.bus2.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.UserService;
import kroryi.bus2.utils.JwtTokenUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserService userService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // JWT 토큰 검증
                var claims = jwtTokenUtil.parseToken(jwt);
                String userId = claims.getSubject();
                String tokenType = jwtTokenUtil.getTokenType(jwt);

                // ACCESS 토큰인지 확인
                if (!"ACCESS".equals(tokenType)) {
                    log.warn("잘못된 토큰 타입: {}", tokenType);
                    filterChain.doFilter(request, response);
                    return;
                }

                if (StringUtils.hasText(userId)) {
                    // 사용자 정보 조회
                    User user = userService.findByUserId(userId);
                    if (user != null && !user.isWithdraw()) {
                        CustomUserDetails userDetails = new CustomUserDetails(user);

                        // 인증 객체 생성
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // SecurityContext에 인증 정보 설정
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("JWT 인증 성공: {}", userId);
                    } else {
                        log.warn("사용자를 찾을 수 없거나 탈퇴한 사용자: {}", userId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
            // 인증 실패 시에도 요청을 계속 진행 (다른 인증 방식이 있을 수 있음)
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 확인
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 2. 쿠키에서 토큰 확인 (브라우저 직접 접속 시)
        String cookieToken = getTokenFromCookie(request, "vue_accessToken");
        if (StringUtils.hasText(cookieToken)) {
            return cookieToken;
        }

        return null;
    }

    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}