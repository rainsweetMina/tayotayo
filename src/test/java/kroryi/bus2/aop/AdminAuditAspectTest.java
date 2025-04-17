package kroryi.bus2.aop;

import kroryi.bus2.entity.AdminAuditLog;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AdminAuditAspectTest {

    @Test
    void adminActions() {
    }

    @Test
    void logAdminOperation() {
    }


    @Test
    public void test() {
        AdminAuditLog log = AdminAuditLog.builder()
                .adminId("admin")
                .action("create")
                .target("notice")
                .afterValue("some val")
                .beforeValue("")
                .timestamp(LocalDateTime.now())
                .build();
    }
}