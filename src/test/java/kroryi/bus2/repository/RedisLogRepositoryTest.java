package kroryi.bus2.repository;

import kroryi.bus2.entity.RedisLog;
import kroryi.bus2.entity.RedisLogJpa;
import kroryi.bus2.repository.jpa.RedisLogJpaRepository;
import kroryi.bus2.service.RedisLogService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Log4j2
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisLogRepositoryTest {

    @Autowired
    RedisLogJpaRepository redisLogJpaRepository;
    @Autowired
    RedisLogService redisLogService;

    @Test
    @DisplayName("redisBasicTest")
    void redisBasicTest() {
        RedisLog redisLog = RedisLog.builder()
                .id(1L)
                .memoryUsageMb(111.3)
                .connectedClients(10L)
                .requestToday(10L)
                .usedMemory(100L)
                .build();

        System.out.println("redisLog = " + redisLog);
        redisLogService.saveLog(redisLog);

    }

    @Test
    void redisTestGet() {

//        RedisLogJpa redisLogJpa = redisLogService.getLog(1L);
        RedisLog redisLog = redisLogService.getLog(1L);
        log.info(redisLog.getMemoryUsageMb());
    }

}