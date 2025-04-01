package kroryi.bus2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "redis_stat", timeToLive = 300)
public class RedisStat {
    private Long id;
    private LocalDateTime timestamp; // LocalDateTime → String
    private Double memoryUsageMb;

}