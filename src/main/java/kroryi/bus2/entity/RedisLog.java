package kroryi.bus2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
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