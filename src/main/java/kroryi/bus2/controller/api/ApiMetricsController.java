package kroryi.bus2.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.ApiResponseStatDTO;
import kroryi.bus2.service.ApiMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation (summary = "오늘 실시간 성능 추이")
    @GetMapping("/response-time/hourly")
    public ResponseEntity<List<ApiResponseStatDTO>> getHourlyStats() {
        return ResponseEntity.ok(metricsService.getHourlyToday());
    }

}
