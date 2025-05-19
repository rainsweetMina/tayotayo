package kroryi.bus2.service;

import kroryi.bus2.dto.ApiResponseStatDTO;
import kroryi.bus2.entity.ApiLogEntry;
import kroryi.bus2.repository.jpa.ApiLogEntryRepository;
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
                        (Double) r[1]
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


}