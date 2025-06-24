package kroryi.bus2.controller.admin.monitoring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.ErrorCountDTO;
import kroryi.bus2.dto.RequestVolumeDTO;
import kroryi.bus2.service.SystemMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "시스템 모니터링", description = "시스템 성능 메트릭스 API")
@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
@Log4j2
public class SystemMetricsController {

    private final SystemMetricsService systemMetricsService;

    @Operation(summary = "시간별 요청 처리량 조회", description = "오늘의 시간별 요청 처리량을 조회합니다.")
    @GetMapping("/request-volume/hourly")
    public ResponseEntity<List<RequestVolumeDTO>> getHourlyRequestVolume() {
        log.info("시간별 요청 처리량 조회 요청 수신");
        List<RequestVolumeDTO> data = systemMetricsService.getHourlyRequestVolumeToday();
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "일별 요청 처리량 조회", description = "최근 n일간의 일별 요청 처리량을 조회합니다.")
    @GetMapping("/request-volume/daily")
    public ResponseEntity<List<RequestVolumeDTO>> getDailyRequestVolume(
            @RequestParam(defaultValue = "7") int days) {
        log.info("일별 요청 처리량 조회 요청 수신 (최근 {}일)", days);
        List<RequestVolumeDTO> data = systemMetricsService.getDailyRequestVolume(days);
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "시간별 에러 발생 건수 조회", description = "오늘의 시간별 에러 발생 건수를 조회합니다.")
    @GetMapping("/errors/hourly")
    public ResponseEntity<List<ErrorCountDTO>> getHourlyErrorCount() {
        log.info("시간별 에러 발생 건수 조회 요청 수신");
        List<ErrorCountDTO> data = systemMetricsService.getHourlyErrorCountToday();
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "일별 에러 발생 건수 조회", description = "최근 n일간의 일별 에러 발생 건수를 조회합니다.")
    @GetMapping("/errors/daily")
    public ResponseEntity<List<ErrorCountDTO>> getDailyErrorCount(
            @RequestParam(defaultValue = "7") int days) {
        log.info("일별 에러 발생 건수 조회 요청 수신 (최근 {}일)", days);
        List<ErrorCountDTO> data = systemMetricsService.getDailyErrorCount(days);
        return ResponseEntity.ok(data);
    }
} 