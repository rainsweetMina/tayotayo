package kroryi.bus2.controller.admin;

import kroryi.bus2.entity.AdminAuditLog;
import kroryi.bus2.repository.jpa.AdminAuditLogRepository;
import kroryi.bus2.service.AuditLogServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminLogPageController {

    private final AuditLogServiceImpl auditLogServiceImpl;
    private final AdminAuditLogRepository adminAuditLogRepository;

    @GetMapping("/admin/logs")
    public String viewAdminLogs(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminAuditLog> logs = auditLogServiceImpl.getLogs(pageable);

        model.addAttribute("logs", logs.getContent());          // 로그 목록
        model.addAttribute("currentPage", logs.getNumber());    // 현재 페이지 번호
        model.addAttribute("totalPages", logs.getTotalPages()); // 전체 페이지 수

        return "admin/admin-logs"; // admin-logs.html로 렌더링
    }



}