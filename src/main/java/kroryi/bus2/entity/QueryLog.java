package kroryi.bus2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class QueryLog {
    // 노선, 정류장 조회 기록(관리자 대쉬보드용)
    @Id @GeneratedValue
    private Long id;

    private String type;
    private String name;
    private LocalDateTime timestamp;


}
