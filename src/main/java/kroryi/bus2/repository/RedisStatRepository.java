package kroryi.bus2.repository;

import kroryi.bus2.entity.RedisStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RedisStatRepository extends JpaRepository<RedisStat, Long> {
    Optional<RedisStat> findTopByOrderByTimestampDesc(); // Redis 최신 1개 사용량
    List<RedisStat> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
