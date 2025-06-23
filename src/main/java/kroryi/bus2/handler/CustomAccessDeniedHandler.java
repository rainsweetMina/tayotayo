package kroryi.bus2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        String uri = request.getRequestURI();
        log.warn("⛔ [접근 거부] URI: {}", uri);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json; charset=UTF-8");

        String message = "🚫 BUS 권한의 사용자에게는 해당 페이지 접근 권한이 없습니다. 분실물/습득물 관리만 가능합니다.";

        String json = String.format("""
            {
              "success": false,
              "message": "%s"
            }
            """, message);

        response.getWriter().write(json);
    }
}
