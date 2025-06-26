package kroryi.bus2.controller.admin.monitoring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.ApiResponseStatDTO;
import kroryi.bus2.service.ApiMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag (name = "API 차트", description = "API 응답 시간 통계")
@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
public class ApiMetricsController {

    private final ApiMetricsService metricsService;

    @Operation (summary = "최근 7일치의 API 응답 시간 통계")
    @GetMapping("/response-time")
    public ResponseEntity<List<ApiResponseStatDTO>> getStats() {
        return ResponseEntity.ok(metricsService.getStats(7)); // 최근 7일
    }

    @Operation (summary = "오늘 시간별 응답 시간 통계")
    @GetMapping("/response-time/hourly")
    public ResponseEntity<List<ApiResponseStatDTO>> getHourlyStats() {
        return ResponseEntity.ok(metricsService.getHourlyToday());
    }
    
    @Operation (summary = "오늘 5분 단위 응답 시간 통계")
    @GetMapping("/response-time/5min")
    public ResponseEntity<List<ApiResponseStatDTO>> getFiveMinuteStats() {
        return ResponseEntity.ok(metricsService.getFiveMinuteIntervalsToday());
    }
    
    @Operation (summary = "최근 N시간 동안 5분 단위 응답 시간 통계")
    @GetMapping("/response-time/5min/{hours}")
    public ResponseEntity<List<ApiResponseStatDTO>> getFiveMinuteStatsForHours(@PathVariable int hours) {
        // 시간 범위 제한 (최대 24시간)
        hours = Math.min(hours, 24);
        return ResponseEntity.ok(metricsService.getFiveMinuteIntervals(hours));
    }
}
