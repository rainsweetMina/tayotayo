package kroryi.bus2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Entity
@Data
@RedisHash("ApiLog")
public class ApiLog {
    // API 호출 로그 (관리자 대쉬보드용)
    @Id @GeneratedValue
    private long id;
    private LocalDateTime timestamp;
    private boolean success;
    private String errorMessage;
    private String logMessage;
}
