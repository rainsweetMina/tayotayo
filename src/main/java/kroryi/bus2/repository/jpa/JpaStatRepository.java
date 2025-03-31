package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.RedisStat;
import kroryi.bus2.entity.RedisStatJpa;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaStatRepository extends CrudRepository<RedisStatJpa, Long> {
}
