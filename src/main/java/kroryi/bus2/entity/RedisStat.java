package kroryi.bus2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;


@RedisHash("redis_stat")
@Data
public class RedisStat {
    // Redis 메모리 사용 기록(관리자 대쉬보드용)
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private  double memoryUsageMb;

}
