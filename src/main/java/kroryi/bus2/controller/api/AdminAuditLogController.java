package kroryi.bus2.controller.api;

import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.service.AuditLogServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogServiceImpl auditLogServiceImpl;

    // /api/admin/logs?page=0&size=10
    @GetMapping
    public Page<AdminAuditLog> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return auditLogServiceImpl.getLogs(PageRequest.of(page, size));
    }
}