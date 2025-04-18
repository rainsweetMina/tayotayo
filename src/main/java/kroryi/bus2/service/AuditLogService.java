package kroryi.bus2.service;

import kroryi.bus2.entity.AdminAuditLog;

public interface AuditLogService {
    void logAdminAction(AdminAuditLog log);
}
