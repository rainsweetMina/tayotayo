package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.RedisLogJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRedisLogRepository extends JpaRepository<RedisLogJpa, Long> {


}