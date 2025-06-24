package kroryi.bus2.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.service.SystemMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
@Log4j2
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final SystemMetricsService systemMetricsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 요청 처리
            filterChain.doFilter(requestWrapper, responseWrapper);
            
            // 요청 정보 로깅
            logRequest(requestWrapper, responseWrapper, System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            // 에러 로깅
            logError(requestWrapper, e);
            throw e;
        } finally {
            // 응답 내용 복원
            responseWrapper.copyBodyToResponse();
        }
    }
    
    private void logRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        // API 요청만 로깅 (정적 리소스 제외)
        if (uri.startsWith("/api")) {
            systemMetricsService.saveRequestLog(uri, method, clientIp, userAgent);
            
            if (log.isDebugEnabled()) {
                log.debug("Request: {} {} from {} ({}), duration: {}ms", method, uri, clientIp, userAgent, duration);
            }
        }
    }
    
    private void logError(HttpServletRequest request, Exception e) {
        String uri = request.getRequestURI();
        String errorMessage = e.getMessage();
        String stackTrace = getStackTraceAsString(e);
        Integer statusCode = (e instanceof org.springframework.web.server.ResponseStatusException) ? 
                ((org.springframework.web.server.ResponseStatusException) e).getStatusCode().value() : 500;
        String errorType = e.getClass().getSimpleName();
        
        // API 요청 에러만 로깅
        if (uri.startsWith("/api")) {
            systemMetricsService.saveErrorLog(uri, errorMessage, stackTrace, statusCode, errorType);
            log.error("Error processing request {}: {}", uri, errorMessage, e);
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            if (sb.length() > 1000) {
                sb.append("... (truncated)");
                break;
            }
        }
        return sb.toString();
    }
} 