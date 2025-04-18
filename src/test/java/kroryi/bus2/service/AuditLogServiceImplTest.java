package kroryi.bus2.service;

import jakarta.transaction.Transactional;
import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.repository.jpa.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuditLogServiceImplTest {

    @Autowired
    private AuditLogServiceImpl auditLogServiceImpl;

    @Autowired
    private AdminAuditLogRepository adminAuditLogRepository;

    @Test
    void logAdminAction_정상_저장됨() {
        // given
        AdminAuditLog log = AdminAuditLog.builder()
                .adminId("admin")
                .action("공지 등록")
                .target("NoticeServiceImpl")
                .beforeValue("{}")
                .afterValue("{\"title\":\"test\"}")
                .timestamp(LocalDateTime.now())
                .build();

        // when
        auditLogServiceImpl.logAdminAction(log);

        // then
        List<AdminAuditLog> logs = adminAuditLogRepository.findAll();
        assertFalse(logs.isEmpty());
        assertEquals("공지 등록", logs.get(0).getAction());
        assertEquals("admin", logs.get(0).getAdminId());
    }


    @Test
    void logAdminAction_등록_저장_확인() {
        // given
        AdminAuditLog log = AdminAuditLog.builder()
                .adminId("admin_test")
                .action("공지 등록")
                .target("notice:123")
                .beforeValue("{}")
                .afterValue("{\"title\":\"test notice\"}")
                .timestamp(LocalDateTime.now())
                .build();

        // when
        auditLogServiceImpl.logAdminAction(log);

        // then
        List<AdminAuditLog> logs = adminAuditLogRepository.findAll();

        boolean logExists = logs.stream().anyMatch(l ->
                "공지 등록".equals(l.getAction()) &&
                        "admin_test".equals(l.getAdminId()) &&
                        "notice:123".equals(l.getTarget())
        );

        assertTrue(logExists, "등록된 로그가 존재하지 않음");
    }

}