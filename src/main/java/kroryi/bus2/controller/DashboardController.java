package kroryi.bus2.controller;


import kroryi.bus2.dto.lost.LostStatResponseDTO;
import kroryi.bus2.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ds")
@RequiredArgsConstructor
@Log4j2
public class DashboardController {

    private final DashboardService dashboardService;


    // dashboard.html 페이지를 반환하는 메서드
    @GetMapping
    public String getDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

//    @GetMapping("/redis-memory")
//    public ResponseEntity<List<Map<String, Object>>> getRedisMemoryStats() {
//        return ResponseEntity.ok(dashboardService.getRedisMemoryStats());
//    }

    // 🔵 관리자용 분실물 통계 API
    @GetMapping("/lost-stat")
    public LostStatResponseDTO getLostStat() {
        return dashboardService.getLostStats();
    }






}
