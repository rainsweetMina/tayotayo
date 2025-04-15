package kroryi.bus2.controller.api;

import kroryi.bus2.dto.ApiResponseStatDTO;
import kroryi.bus2.service.ApiMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
public class ApiMetricsController {

    private final ApiMetricsService metricsService;

    @GetMapping("/response-time")
    public ResponseEntity<List<ApiResponseStatDTO>> getStats() {
        return ResponseEntity.ok(metricsService.getStats(7)); // 최근 7일
    }

    @GetMapping("/response-time/hourly")
    public ResponseEntity<List<ApiResponseStatDTO>> getHourlyStats() {
        return ResponseEntity.ok(metricsService.getHourlyToday());
    }

}
