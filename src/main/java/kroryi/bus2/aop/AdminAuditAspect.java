package kroryi.bus2.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.repository.jpa.admin.AdminAuditLogRepository;
import kroryi.bus2.service.AuditLogServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuditAspect {

    @Autowired
    private final AuditLogServiceImpl auditLogServiceImpl;
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
            throw e; // 예외를 다시 던져서 원래의 예외를 유지
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

//            auditLogServiceImpl.logAdminAction(
//                    adminAudit.action(),
//                    adminAudit.target(),
//                    argsJson,
//                    resultJson
//            );
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
