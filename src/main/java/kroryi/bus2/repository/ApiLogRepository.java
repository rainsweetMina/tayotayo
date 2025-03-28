package kroryi.bus2.repository;

import kroryi.bus2.entity.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {
    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
