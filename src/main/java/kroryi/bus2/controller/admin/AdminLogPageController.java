package kroryi.bus2.controller.admin;

import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.service.AuditLogServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminLogPageController {

    private final AuditLogServiceImpl auditLogServiceImpl;

    @GetMapping("/admin/logs")
    public String adminLogsPage(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<AdminAuditLog> logs = auditLogServiceImpl.getLogs(PageRequest.of(page, 10));
        model.addAttribute("logs", logs);

        return "/admin/admin-logs";
    }
}