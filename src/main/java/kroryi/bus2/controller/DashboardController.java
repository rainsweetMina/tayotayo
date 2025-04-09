package kroryi.bus2.controller;


import kroryi.bus2.dto.lost.LostStatResponseDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/ds")
@RequiredArgsConstructor
@Log4j2
public class DashboardController {

    private final DashboardService dashboardService;
    private final RedisLogService redisLogService;
    private final RouteLogService routeLogService;
    @Autowired
    private NoticeService noticeService;

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


    // 공지사항 페이지
//    @GetMapping("/notices")
//    public String getNotices() {
//        return "dashboard/notice";
//    }
//
//    // 공지사항 목록 조회
//    @GetMapping("/notices")
//    public List<Notice> getAllNotices() {
//        return noticeService.getAllNotices();
//    }
//
//    // 공지사항 추가
//    @PostMapping("/notices")
//    public ResponseEntity<Notice> addNotice(@RequestBody Notice notice) {
//        return ResponseEntity.ok(noticeService.addNotice(notice));
//    }
//
//    // 공지사항 수정
//    @PutMapping("/notices/{id}")
//    public ResponseEntity<Notice> updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
//        return ResponseEntity.ok(noticeService.updateNotice(id, notice));
//    }
//
//    // 공지사항 삭제
//    @DeleteMapping("/notices/{id}")
//    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
//        noticeService.deleteNotice(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    // 공지사항 상세 조회
//    @GetMapping("/notices/{id}")
//    public ResponseEntity<Notice> getNoticeById(@PathVariable Long id) {
//        return ResponseEntity.ok(noticeService.getNoticeById(id));
//    }


    // Redis 상태 정보를 반환하는 메서드 추후 변경 예정
//    @GetMapping("/redis")
//    public ResponseEntity<Map<String, String>> getRedisStats() {
//        Map<String, String> redisStats = routeLogService.getRedisInfo();
//        return ResponseEntity.ok(redisStats);
//    }

}
