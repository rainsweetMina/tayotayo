package kroryi.bus2.scheduler;

import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.repository.jpa.AdminAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuditLogCleanerTest {

    @Autowired
    private AuditLogCleaner cleaner;

    @Autowired
    private AdminAuditLogRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        // 어제 날짜 (삭제 대상)
        repository.save(AdminAuditLog.builder()
                .adminId("admin")
                .action("삭제 대상")
                .target("old")
                .beforeValue("{}")
                .afterValue("{}")
                .timestamp(LocalDateTime.now().minusDays(2))
                .build());

        // 오늘 날짜 (삭제 X)
        repository.save(AdminAuditLog.builder()
                .adminId("admin")
                .action("유지 대상")
                .target("recent")
                .beforeValue("{}")
                .afterValue("{}")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Test
    void deleteOldLogs_하루지난_로그만_삭제됨() {
        // given
        assertEquals(2, repository.count());

        // when
        cleaner.deleteOldLogs(); // 수동 실행

        // then
        List<AdminAuditLog> remaining = repository.findAll();
        assertEquals(1, remaining.size());
        assertEquals("유지 대상", remaining.get(0).getAction());
    }
}