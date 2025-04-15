package kroryi.bus2.controller.admin;


import kroryi.bus2.dto.lost.LostStatResponseDTO;
import kroryi.bus2.service.*;
import kroryi.bus2.service.admin.DashboardService;
import kroryi.bus2.service.admin.RedisLogService;
import kroryi.bus2.service.admin.notice.NoticeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ds")
@RequiredArgsConstructor
@Log4j2
public class DashboardController {

    private final DashboardService dashboardService;
    private final RedisLogService redisLogService;
    private final RouteLogService routeLogService;
    @Autowired
    private NoticeServiceImpl noticeService;

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
