package kroryi.bus2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RedisStat {
    // Redis 메모리 사용 기록(관리자 대쉬보드용)
    @jakarta.persistence.Id
    @GeneratedValue
    private Long id;

    private LocalDateTime timestamp;
    private double memoryUsageMb;

}
