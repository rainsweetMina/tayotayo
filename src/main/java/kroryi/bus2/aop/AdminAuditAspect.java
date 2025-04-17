package kroryi.bus2.aop;

import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.service.AuditLogServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Aspect
@Component
public class AdminAuditAspect {

    @Autowired
    private AuditLogServiceImpl auditLogServiceImpl;

    @Pointcut("execution(* kroryi..*.create*(..)) || execution(* kroryi..*.update*(..)) || execution(* kroryi..*.delete*(..)) || @annotation(AdminTracked)")
    public void adminActions() {}

    @Around("adminActions()")
    public Object logAdminOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        String action = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 🚫 Redis 등 제외
        if (className.contains("Redis")) return joinPoint.proceed();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal().equals("anonymousUser")) return joinPoint.proceed();

        String adminId = auth.getName();
        StringBuilder argInfo = new StringBuilder();

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof MultipartFile || (arg instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof MultipartFile)) {
                argInfo.append("[파일첨부 생략], ");
            } else {
                argInfo.append(arg).append(", ");
            }
        }

        try {
            result = joinPoint.proceed();

            auditLogServiceImpl.logAdminAction(AdminAuditLog.builder()
                    .adminId(adminId)
                    .action(action)
                    .target(className)
                    .beforeValue("") // 필요 시 비교
                    .afterValue(argInfo.toString())
                    .timestamp(LocalDateTime.now())
                    .build());

            return result;

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
    @AfterReturning(value = "@annotation(adminAudit)", returning = "result")
    public void logAdminAudit(JoinPoint joinPoint, AdminAudit adminAudit, Object result) {
        try {
            String adminId = getCurrentAdminUsername();
            if ("anonymous".equals(adminId)) return;

            String argsJson = objectMapper.writeValueAsString(joinPoint.getArgs());
            String resultJson = objectMapper.writeValueAsString(result);

            auditLogService.logAdminAction(
                    adminAudit.action(),
                    adminAudit.target(),
                    argsJson,
                    resultJson
            );

            log.info("[🛡️ AdminAudit] {} - {} by {}", adminAudit.action(), adminAudit.target(), adminId);
        } catch (Exception e) {
            log.error("🚨 AdminAudit 기록 실패", e);
        }
    }

}
