package kroryi.bus2.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@RedisHash(value = "redisLog", timeToLive = 300)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RedisLogJpa implements Serializable {

    @Id
    public Long id;
    private Long usedMemory;
    private Long connectedClients;
    private Long routesCount;
    private Long requestToday;
    private LocalDateTime timestamp;
    private Double memoryUsageMb;

}