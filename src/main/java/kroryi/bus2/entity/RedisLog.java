package kroryi.bus2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;


@RedisHash(value = "redisLog", timeToLive = 300)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RedisLog {

    @Id
    public Long id;
    private Long usedMemory;
    private Long connectedClients;
    private Long routesCount;
    private Long requestToday;
    private LocalDateTime timestamp;
    private Double memoryUsageMb;

}