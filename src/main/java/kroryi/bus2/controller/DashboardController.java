package kroryi.bus2.controller;


import kroryi.bus2.dto.lost.LostStatResponseDTO;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.repository.redis.RedisRouteRepository;
import kroryi.bus2.service.DashboardService;
import kroryi.bus2.service.DayStatsService;
import kroryi.bus2.service.RedisLogService;
import kroryi.bus2.service.RouteLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final RedisLogService redisLogService;
    private final RouteLogService routeLogService;


    // dashboard.html 페이지를 반환하는 메서드
    @GetMapping
    public String getDashboard() {
        return "admin/dashboard";
    }


    // 🔵 관리자용 분실물 통계 API
    @GetMapping("/lost-stat")
    public LostStatResponseDTO getLostStat() {
        return dashboardService.getLostStats();
    }

    // Redis 상태 정보를 반환하는 메서드 추후 변경 예정
//    @GetMapping("/redis")
//    public ResponseEntity<Map<String, String>> getRedisStats() {
//        Map<String, String> redisStats = routeLogService.getRedisInfo();
//        return ResponseEntity.ok(redisStats);
//    }

}
