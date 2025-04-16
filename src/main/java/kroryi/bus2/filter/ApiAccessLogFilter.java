package kroryi.bus2.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import kroryi.bus2.entity.ApiAccessLog;
import kroryi.bus2.repository.jpa.ApiAccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ApiAccessLogFilter extends OncePerRequestFilter {

    private final ApiAccessLogRepository apiAccessLogRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.currentTimeMillis(); // 응답 시작 시간 측정
        filterChain.doFilter(request, response); // 요청 처리 진행
        long duration = System.currentTimeMillis() - start; // 처리 후 소요시간 계산

        // 🔐 현재 인증된 사용자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 사용자 ID 추출
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";

        // 권한 중 'ADMIN' 포함 여부로 관리자 판별
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (isAdmin) {
            // 로그 객체 생성
            ApiAccessLog log = ApiAccessLog.builder()
                    .uri(request.getRequestURI())
                    .method(request.getMethod())
                    .status(response.getStatus())
                    .durationMs(duration)
                    .ip(request.getRemoteAddr())
                    .username(username)
                    .timestamp(LocalDateTime.now())
                    .build();

            apiAccessLogRepository.save(log); // DB에 저장
        }
    }
}