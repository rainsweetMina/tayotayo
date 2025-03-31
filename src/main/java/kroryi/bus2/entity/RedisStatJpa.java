package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "redis_stat")
@Data
public class RedisStatJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private double memoryUsageMb;

    @Version
    private Long version =0L;  // 낙관적 잠금 필드
}