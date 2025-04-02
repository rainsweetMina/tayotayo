package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.RedisLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisLogJpaRepository extends JpaRepository<RedisLog, Long> {


}