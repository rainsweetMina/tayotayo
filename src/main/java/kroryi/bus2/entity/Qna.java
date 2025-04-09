package kroryi.bus2.entity;

import jakarta.persistence.*;
import kroryi.bus2.entity.user.User;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Setter
    @Enumerated(EnumType.STRING)
    private QnaStatus status; // WAITING, ANSWERED

    @Setter
    @Column(columnDefinition = "TEXT")
    private String answer; // 관리자 답변

    private boolean isSecret;   // 비공개 여부
    private boolean isDeleted;  // 사용자 soft delete
    private boolean visible;    // 관리자 숨김 처리

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}


