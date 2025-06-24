package kroryi.bus2.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.PostsStatsDTO;
import kroryi.bus2.dto.UserStatsDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.admin.AdminStatsService;
import kroryi.bus2.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "관리자-통계", description = "관리자 대시보드용 통계 API")
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Log4j2
public class AdminStatsController {

    private final AdminStatsService adminStatsService;
    private final AdminUserService adminUserService;

    @Operation(summary = "게시글 통계 조회", description = "각 게시판의 오늘 등록된 게시물 수와 전체 게시물 수를 반환합니다.")
    @GetMapping("/posts")
    public ResponseEntity<PostsStatsDTO> getPostsStats() {
        log.info("게시글 통계 요청 수신");
        PostsStatsDTO stats = adminStatsService.getPostsStats();
        return ResponseEntity.ok(stats);
    }
    
    @Operation(summary = "회원 통계 조회", description = "전체 회원 수, 오늘 가입한 회원 수, 회원 증가율을 반환합니다.")
    @GetMapping("/users")
    public ResponseEntity<UserStatsDTO> getUserStats() {
        log.info("회원 통계 요청 수신");
        UserStatsDTO stats = adminUserService.getUserStats();
        log.info("회원 통계 응답: totalUsers={}, newUsersToday={}, increaseRate={}, usersByType={}", 
                 stats.getTotalUsers(), stats.getNewUsersToday(), stats.getIncreaseRate(), stats.getUsersByType());
        return ResponseEntity.ok(stats);
    }
    
    @Operation(summary = "오늘 가입한 회원 디버그", description = "오늘 가입한 회원 정보를 디버그합니다.")
    @GetMapping("/users/debug")
    public ResponseEntity<Map<String, Object>> debugTodayUsers() {
        log.info("오늘 가입한 회원 디버그 요청");
        
        // 오늘 가입한 회원 목록
        List<User> todayUsers = adminUserService.getTodaySignupUsers();
        
        // 오늘 날짜 범위
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);
        
        Map<String, Object> debugInfo = Map.of(
            "today", today.toString(),
            "startOfDay", startOfDay.toString(),
            "endOfDay", endOfDay.toString(),
            "todayUsersCount", todayUsers.size(),
            "todayUsers", todayUsers.stream().map(user -> 
                Map.of(
                    "id", user.getId(),
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "signupDate", user.getSignupDate() != null ? user.getSignupDate().toString() : "null",
                    "role", user.getRole().toString()
                )
            ).toList()
        );
        
        log.info("오늘 가입한 회원 수: {}", todayUsers.size());
        return ResponseEntity.ok(debugInfo);
    }
}
 