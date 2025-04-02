package kroryi.bus2.repository.redis;

import kroryi.bus2.entity.RedisLogJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisLogRepository extends JpaRepository<RedisLogJpa, Long> {


}