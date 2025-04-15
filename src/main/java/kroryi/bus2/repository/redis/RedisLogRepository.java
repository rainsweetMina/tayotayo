package kroryi.bus2.repository.redis;

import kroryi.bus2.entity.RedisLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RedisLogRepository extends JpaRepository<RedisLog, Long> {

    @Query("SELECT DATE(r.timestamp) as day, AVG(r.memoryUsageMb) as avgMemory " +
            "FROM RedisLogJpa r " +
            "WHERE r.timestamp BETWEEN :start AND :end " +
            "GROUP BY DATE(r.timestamp) " +
            "ORDER BY day ASC")
    List<Object[]> findDailyAvgMemory(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);
}