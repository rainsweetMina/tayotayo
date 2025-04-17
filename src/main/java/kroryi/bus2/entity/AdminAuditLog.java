package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adminId;         // 사용자 아이디
    private String action;          // 예: "공지 수정", "공지 삭제"
    private String target;          // 예: "notice:5"

    @Lob
    private String beforeValue;     // 수정 전 (JSON)

    @Lob
    private String afterValue;      // 수정 후 (JSON)

    private LocalDateTime timestamp;

}
