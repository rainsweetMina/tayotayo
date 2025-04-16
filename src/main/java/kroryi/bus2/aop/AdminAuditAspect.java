package kroryi.bus2.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import kroryi.bus2.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuditAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;



    // ✅ 관리자 서비스 메서드 (등록/수정/삭제 등)를 감지
    @AfterReturning(
            pointcut = "execution(* kroryi.bus2.service..*.create*(..)) || " +
                    "execution(* kroryi.bus2.service..*.update*(..)) || " +
                    "execution(* kroryi.bus2.service..*.delete*(..)) || " +
                    "@annotation(kroryi.bus2.aop.AdminTracked)",
            returning = "result"
    )
    public void logAdminOperation(JoinPoint joinPoint, Object result) {
        try {
            String adminId = getCurrentAdminUsername();
            if ("anonymous".equals(adminId)) return; // ✅ 익명 사용자 제외

            String className = joinPoint.getTarget().getClass().getSimpleName();
            if (className.contains("Redis")) return; // 🔥 Redis 클래스 제외

            String methodName = joinPoint.getSignature().getName();
            String action = resolveAction(methodName);
            String target = className + "#" + methodName;

            String afterJson = objectMapper.writeValueAsString(result);
            String argsJson = objectMapper.writeValueAsString(joinPoint.getArgs());

            auditLogService.logAdminAction(
                    action,
                    target,
                    argsJson,
                    afterJson
            );

            log.info("[AOP AUDIT] {} - {} by {}", action, target, adminId);

        } catch (Exception e) {
            log.error("🚨 관리자 작업 로그 기록 실패", e);
        }
    }

    private String getCurrentAdminUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    private String resolveAction(String method) {
        if (method.startsWith("create")) return "등록";
        if (method.startsWith("update")) return "수정";
        if (method.startsWith("delete")) return "삭제";
        return "작업";
    }
}
