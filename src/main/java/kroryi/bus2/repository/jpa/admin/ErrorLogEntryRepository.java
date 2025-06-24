package kroryi.bus2.repository.jpa.admin;

import kroryi.bus2.entity.ErrorLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ErrorLogEntryRepository extends JpaRepository<ErrorLogEntry, Long> {

    // 시간별 에러 발생 건수를 조회하는 쿼리
    @Query("SELECT FUNCTION('DATE_FORMAT', e.timestamp, '%H:00') as hour, COUNT(e) " +
            "FROM ErrorLogEntry e " +
            "WHERE DATE(e.timestamp) = CURRENT_DATE " +
            "GROUP BY FUNCTION('DATE_FORMAT', e.timestamp, '%H:00') " +
            "ORDER BY hour")
    List<Object[]> findHourlyErrorCountToday();
    
    // 일자별 에러 발생 건수를 조회하는 쿼리
    @Query("SELECT DATE(e.timestamp) as date, COUNT(e) " +
            "FROM ErrorLogEntry e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "GROUP BY DATE(e.timestamp) " +
            "ORDER BY date")
    List<Object[]> findDailyErrorCount(LocalDateTime start, LocalDateTime end);
} 