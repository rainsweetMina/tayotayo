package kroryi.bus2.repository.redis;

import kroryi.bus2.entity.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;

@Repository
public interface ApiLogRepository extends CrudRepository<ApiLog, Long> {
    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);
}