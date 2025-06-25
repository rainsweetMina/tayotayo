package kroryi.bus2.repository.jpa.admin;

import kroryi.bus2.entity.ApiLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ApiLogEntryRepository extends JpaRepository<ApiLogEntry, Long> {

    // 일자별 평균 응답시간을 구하는 쿼리
    @Query("SELECT DATE(a.timestamp), AVG(a.responseTimeMs) " +
            "FROM ApiLogEntry a " +
            "WHERE a.timestamp BETWEEN :start AND :end " +
            "GROUP BY DATE(a.timestamp) " +
            "ORDER BY DATE(a.timestamp)")
    List<Object[]> findDailyAvgResponse(LocalDateTime start, LocalDateTime end);

    // 시간대별 평균 응답시간을 구하는 쿼리
    @Query("SELECT FUNCTION('DATE_FORMAT', a.timestamp, '%H:00'), AVG(a.responseTimeMs) " +
            "FROM ApiLogEntry a " +
            "WHERE DATE(a.timestamp) = CURRENT_DATE " +
            "GROUP BY FUNCTION('DATE_FORMAT', a.timestamp, '%H:00') " +
            "ORDER BY FUNCTION('DATE_FORMAT', a.timestamp, '%H:00')")
    List<Object[]> findHourlyAvgToday();

    // 5분 단위로 평균 응답시간을 구하는 쿼리 (수정)
    @Query(value = "SELECT DATE_FORMAT(timestamp, '%H:%i') AS time_slot, AVG(response_time_ms) " +
            "FROM api_log_entry " +
            "WHERE DATE(timestamp) = CURRENT_DATE " +
            "AND timestamp >= :startTime " +
            "GROUP BY DATE_FORMAT(timestamp, '%H:%i') " +
            "ORDER BY time_slot", nativeQuery = true)
    List<Object[]> findByFiveMinuteIntervalsToday(LocalDateTime startTime);
    
    // 최근 N시간 동안 5분 단위로 평균 응답시간을 구하는 쿼리 (수정)
    @Query(value = "SELECT DATE_FORMAT(timestamp, '%H:%i') AS time_slot, AVG(response_time_ms) " +
            "FROM api_log_entry " +
            "WHERE timestamp >= :startTime " +
            "GROUP BY DATE_FORMAT(timestamp, '%H:%i') " +
            "ORDER BY time_slot", nativeQuery = true)
    List<Object[]> findByFiveMinuteIntervals(LocalDateTime startTime);
}