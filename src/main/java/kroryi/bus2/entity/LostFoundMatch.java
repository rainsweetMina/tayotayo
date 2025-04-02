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
public class LostFoundMatch {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private LostItem lostItem;

    @OneToOne
    private FoundItem foundItem;

    private LocalDateTime matchedAt;

    // 🟢 매칭 등록자 → 버스회사 관리자
    @ManyToOne
    private User matchedBy;
}

