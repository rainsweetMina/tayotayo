package kroryi.bus2.repository;

import kroryi.bus2.entity.RedisStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RedisStatRepository extends JpaRepository<RedisStat, Long> {
    Optional<RedisStat> findTopByOrderByTimestampDesc(); // Redis 최근 사용량
}
