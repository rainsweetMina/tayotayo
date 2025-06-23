package kroryi.bus2.controller.admin.monitoring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.PostsStatsDTO;
import kroryi.bus2.service.admin.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 통계", description = "관리자 대시보드용 통계 API")
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @Operation(summary = "게시물 통계 조회", description = "공지사항, Q&A, FAQ, 광고 등의 게시물 통계를 조회합니다.")
    @GetMapping("/posts")
    public ResponseEntity<PostsStatsDTO> getPostsStats() {
        return ResponseEntity.ok(adminStatsService.getPostsStats());
    }
} 