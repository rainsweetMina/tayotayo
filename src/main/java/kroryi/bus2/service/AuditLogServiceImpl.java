package kroryi.bus2.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.repository.jpa.admin.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void logAdminAction(AdminAuditLog log) {
        adminAuditLogRepository.save(log);
    }


    public void logAdminAction(String action, String target, Object before, Object after) {

        try {
            String adminId = getCurrentAdminUsername(); // ⬅ 관리자 ID 자동 추출

            AdminAuditLog log = AdminAuditLog.builder()
                    .adminId(adminId)
                    .action(action)
                    .target(target)
                    .beforeValue(before != null ? objectMapper.writeValueAsString(before) : null)
                    .afterValue(after != null ? objectMapper.writeValueAsString(after) : null)
                    .timestamp(LocalDateTime.now())
                    .build();

            adminAuditLogRepository.save(log);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("감사 로그 직렬화 실패", e);
        }
    }

    // 🔐 현재 로그인한 사용자 이름 추출
    private String getCurrentAdminUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    // 관리자 로그 조회
    public Page<AdminAuditLog> getLogs(Pageable pageable) {
        return adminAuditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }


}
