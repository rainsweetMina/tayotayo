package kroryi.bus2.repository;


import kroryi.bus2.entity.RedisStat;
import kroryi.bus2.entity.RedisStatJpa;
import kroryi.bus2.repository.jpa.JpaStatRepository;
import kroryi.bus2.repository.jpa.RouteRepository;
import kroryi.bus2.repository.redis.ApiLogRepository;
import kroryi.bus2.repository.redis.RedisStatRepository;
import kroryi.bus2.service.RedisSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;


@SpringBootTest
@RequiredArgsConstructor
@Log4j2
public class RedisRepositoryTests {
    @Autowired
    public RedisSyncService redisSyncService;
    @Autowired
    private RedisStatRepository redisStatRepository;

    @Test
    public void testRedisRepository() {
        // 데이터 삽입

        RedisStatJpa redisStat = redisSyncService.getStat(1L);
        if (redisStat != null) {
            log.info("1111111111111111111111 {}", redisStat);
            log.info(redisStat.toString());
        }else{
            RedisStatJpa stat = new RedisStatJpa();
            stat.setId(2L);
            stat.setTimestamp(LocalDateTime.now());
            stat.setMemoryUsageMb(123.45);
            redisSyncService.saveStat(stat);
        }

    }

    @Test
    public void testJpaRedisRepository() {
        // 데이터 삽입
        RedisStat stat = new RedisStat();
        stat.setId(1L);
        stat.setTimestamp(LocalDateTime.now());
        stat.setMemoryUsageMb(123.45);
        redisStatRepository.save(stat);

        // 데이터 조회
        List<RedisStat> stats = (List<RedisStat>) redisStatRepository.findAll();
        log.info("RedisStat count: {}", stats.size());
        stats.forEach(s -> log.info("RedisStat: {}", s));
    }
}
