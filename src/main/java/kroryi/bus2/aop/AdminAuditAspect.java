package kroryi.bus2.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.repository.jpa.AdminAuditLogRepository;
import kroryi.bus2.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuditAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final AdminAuditLogRepository logRepository;




    // ✅ 관리자 서비스 메서드 (등록/수정/삭제 등)를 감지
    @AfterReturning(
            pointcut = "execution(* kroryi.bus2.service..*.create*(..)) || " +
                    "execution(* kroryi.bus2.service..*.update*(..)) || " +
                    "execution(* kroryi.bus2.service..*.delete*(..)) || " +
                    "@annotation(kroryi.bus2.aop.AdminTracked)",
            returning = "result"
    )
    public void logTrackedOperation(JoinPoint joinPoint, Object result) {
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
    @AfterReturning(value = "@annotation(adminAudit)", returning = "result")
    public void logAuditedOperation(JoinPoint joinPoint, AdminAudit adminAudit, Object result) {
        try {
            String adminId = getCurrentAdminUsername();
            String action = adminAudit.action();
            String target = adminAudit.target();

            Map<String, Object> paramMap = new LinkedHashMap<>();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                String key = "arg" + i;

                if (arg instanceof MultipartFile) {
                    paramMap.put(key, "[파일 업로드 생략]");
                } else if (arg instanceof FoundItemRequestDTO dto) {
                    Map<String, Object> safeDto = new LinkedHashMap<>();
                    safeDto.put("itemName", dto.getItemName());
                    safeDto.put("busCompany", dto.getBusCompany());
                    safeDto.put("busNumber", dto.getBusNumber());
                    safeDto.put("foundPlace", dto.getFoundPlace());
                    safeDto.put("foundTime", dto.getFoundTime());
                    safeDto.put("content", dto.getContent());
                    safeDto.put("storageLocation", dto.getStorageLocation());
                    safeDto.put("handlerContact", dto.getHandlerContact());
                    safeDto.put("handlerEmail", dto.getHandlerEmail());
                    safeDto.put("status", dto.getStatus());
                    safeDto.put("handlerId", dto.getHandlerId());
                    paramMap.put("FoundItemRequestDTO", safeDto);
                } else {
                    try {
                        paramMap.put(key, objectMapper.writeValueAsString(arg));
                    } catch (Exception e) {
                        paramMap.put(key, "[직렬화 실패]");
                    }
                }
            }

            AdminAuditLog auditLog = AdminAuditLog.builder()
                    .adminId(adminId)
                    .action(action)
                    .target(target)
                    .beforeValue(null)
                    .afterValue(objectMapper.writeValueAsString(paramMap))
                    .timestamp(LocalDateTime.now())
                    .build();

            logRepository.save(auditLog);
            log.info("[AUDIT ✅] {} - {} by {}", action, target, adminId);


        } catch (Exception e) {
            log.error("🚨 관리자 작업 로그 기록 실패", e);
        }
    }



}
