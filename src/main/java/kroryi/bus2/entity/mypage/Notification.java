package kroryi.bus2.entity.mypage;

import jakarta.persistence.*;
import kroryi.bus2.entity.user.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;         // 알림 제목
    private String message;       // 알림 본문

    @Column(name = "is_read")
    @Builder.Default
    private boolean read = false; // 읽음 여부

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 외래키
    private User user;
}
