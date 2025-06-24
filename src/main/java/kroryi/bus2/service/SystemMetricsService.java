package kroryi.bus2.service;

import kroryi.bus2.dto.ErrorCountDTO;
import kroryi.bus2.dto.RequestVolumeDTO;
import kroryi.bus2.entity.ErrorLogEntry;
import kroryi.bus2.entity.RequestLogEntry;
import kroryi.bus2.repository.jpa.admin.ErrorLogEntryRepository;
import kroryi.bus2.repository.jpa.admin.RequestLogEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class SystemMetricsService {

    private final RequestLogEntryRepository requestLogEntryRepository;
    private final ErrorLogEntryRepository errorLogEntryRepository;

    /**
     * 요청 로그를 저장합니다.
     */
    public void saveRequestLog(String uri, String method, String clientIp, String userAgent) {
        RequestLogEntry entry = RequestLogEntry.builder()
                .uri(uri)
                .method(method)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();
        
        requestLogEntryRepository.save(entry);
    }

    /**
     * 에러 로그를 저장합니다.
     */
    public void saveErrorLog(String uri, String errorMessage, String stackTrace, 
                            Integer statusCode, String errorType) {
        ErrorLogEntry entry = ErrorLogEntry.builder()
                .uri(uri)
                .errorMessage(errorMessage)
                .stackTrace(stackTrace)
                .statusCode(statusCode)
                .errorType(errorType)
                .timestamp(LocalDateTime.now())
                .build();
        
        errorLogEntryRepository.save(entry);
    }

    /**
     * 오늘의 시간별 요청 처리량을 조회합니다.
     */
    public List<RequestVolumeDTO> getHourlyRequestVolumeToday() {
        List<Object[]> results = requestLogEntryRepository.findHourlyRequestCountToday();
        
        return results.stream()
                .map(row -> new RequestVolumeDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 최근 n일간의 일별 요청 처리량을 조회합니다.
     */
    public List<RequestVolumeDTO> getDailyRequestVolume(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(days);
        
        List<Object[]> results = requestLogEntryRepository.findDailyRequestCount(start, now);
        
        return results.stream()
                .map(row -> new RequestVolumeDTO(
                        ((java.sql.Date) row[0]).toLocalDate().toString(),
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 오늘의 시간별 에러 발생 건수를 조회합니다.
     */
    public List<ErrorCountDTO> getHourlyErrorCountToday() {
        List<Object[]> results = errorLogEntryRepository.findHourlyErrorCountToday();
        
        return results.stream()
                .map(row -> new ErrorCountDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 최근 n일간의 일별 에러 발생 건수를 조회합니다.
     */
    public List<ErrorCountDTO> getDailyErrorCount(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(days);
        
        List<Object[]> results = errorLogEntryRepository.findDailyErrorCount(start, now);
        
        return results.stream()
                .map(row -> new ErrorCountDTO(
                        ((java.sql.Date) row[0]).toLocalDate().toString(),
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }
} 