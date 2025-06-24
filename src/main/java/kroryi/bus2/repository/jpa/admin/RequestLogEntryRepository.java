package kroryi.bus2.repository.jpa.admin;

import kroryi.bus2.entity.RequestLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestLogEntryRepository extends JpaRepository<RequestLogEntry, Long> {

    // 시간별 요청 건수를 조회하는 쿼리
    @Query("SELECT FUNCTION('DATE_FORMAT', r.timestamp, '%H:00') as hour, COUNT(r) " +
            "FROM RequestLogEntry r " +
            "WHERE DATE(r.timestamp) = CURRENT_DATE " +
            "GROUP BY FUNCTION('DATE_FORMAT', r.timestamp, '%H:00') " +
            "ORDER BY hour")
    List<Object[]> findHourlyRequestCountToday();
    
    // 일자별 요청 건수를 조회하는 쿼리
    @Query("SELECT DATE(r.timestamp) as date, COUNT(r) " +
            "FROM RequestLogEntry r " +
            "WHERE r.timestamp BETWEEN :start AND :end " +
            "GROUP BY DATE(r.timestamp) " +
            "ORDER BY date")
    List<Object[]> findDailyRequestCount(LocalDateTime start, LocalDateTime end);
} 