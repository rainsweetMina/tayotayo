package kroryi.bus2.repository;


import kroryi.bus2.service.RedisSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;


@SpringBootTest
@RequiredArgsConstructor
@Log4j2
public class RedisRepositoryTests {
    @Autowired
    public RedisSyncService redisSyncService;

    @Autowired
    public MonitorRedisService monitorRedisService;

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
    public void testRedisRepository2() {

        RedisStat redisStat = monitorRedisService.collectRedisStats(1L);
        log.info("11111111111111111111121 {}", redisStat.getId());
    }

    @Test
    public void testRedisRepositorySave() {

        RedisStatJpa redisStat = RedisStatJpa.builder()
                .id(1L)
                .timestamp(LocalDateTime.now())
                .memoryUsageMb(123.45)
                .usedMemory(112L)
                .connectedClients(1L)
                .requestToday(10L)
                .build();

        monitorRedisService.saveMonitor(redisStat);
        log.info("11111111111111111111123 {}", redisStat.getId());
    }


}
