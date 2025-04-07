package kroryi.bus2.entity;

import jakarta.persistence.*;
import kroryi.bus2.entity.user.User;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String busNumber;

    private LocalDateTime lostTime; // 분실 시각

    @ManyToOne
    private User reporter; // 일반회원 정보 (User 테이블 참조)

    private boolean matched = false; // 매칭 여부

    private boolean visible = true;
}
