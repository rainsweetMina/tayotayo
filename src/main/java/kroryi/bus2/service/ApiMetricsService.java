package kroryi.bus2.service;

import kroryi.bus2.dto.ApiResponseStatDTO;
import kroryi.bus2.entity.ApiLogEntry;
import kroryi.bus2.repository.jpa.admin.ApiLogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiMetricsService {

    private final ApiLogEntryRepository apiLogEntryRepository;

    public void saveLog(String uri, long durationMs) {
        ApiLogEntry entry = ApiLogEntry.builder()
                .uri(uri)
                .responseTimeMs(durationMs)
                .timestamp(LocalDateTime.now())
                .build();

        apiLogEntryRepository.save(entry);
    }

    // 일자별 평균 응답시간을 구하는 메서드
    public List<ApiResponseStatDTO> getStats(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(days);

        return apiLogEntryRepository.findDailyAvgResponse(start, now).stream()
                .map(r -> new ApiResponseStatDTO(
                        ((java.sql.Date) r[0]).toLocalDate().toString(),
                        (double) r[1]
                ))
                .toList();
    }


    // 시간대별 평균 응답시간을 구하는 메서드
    public List<ApiResponseStatDTO> getHourlyToday() {
        return apiLogEntryRepository.findHourlyAvgToday().stream()
                .map(row -> new ApiResponseStatDTO(
                        (String) row[0],
                        ((Number) row[1]).doubleValue()
                ))
                .toList();
    }

    // 오늘 5분 단위 평균 응답시간을 구하는 메서드
    public List<ApiResponseStatDTO> getFiveMinuteIntervalsToday() {
        // 오늘 자정부터 현재까지
        LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        return apiLogEntryRepository.findByFiveMinuteIntervalsToday(startTime).stream()
                .map(row -> new ApiResponseStatDTO(
                        (String) row[0],
                        ((Number) row[1]).doubleValue()
                ))
                .toList();
    }
    
    // 최근 N시간 동안 5분 단위 평균 응답시간을 구하는 메서드
    public List<ApiResponseStatDTO> getFiveMinuteIntervals(int hours) {
        // 현재 시간에서 N시간 전
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        
        return apiLogEntryRepository.findByFiveMinuteIntervals(startTime).stream()
                .map(row -> new ApiResponseStatDTO(
                        (String) row[0],
                        ((Number) row[1]).doubleValue()
                ))
                .toList();
    }
}