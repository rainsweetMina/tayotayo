package kroryi.bus2.entity.lost;

import jakarta.persistence.*;
import kroryi.bus2.entity.user.User;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoundItem extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean deleted;

    private String itemName;
    private String busCompany;
    private String busNumber;
    private String foundPlace;
    private String content;
    private String handlerContact;
    private String handlerEmail;
    private String status;
    private String storageLocation;
    private String photoUrl;

    private LocalDateTime foundTime; // 습득 시각

    @ManyToOne
    private User handler; // 등록한 버스회사 관리자

    private boolean matched = false; // 매칭 여부


    @OneToOne(mappedBy = "foundItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Photo photo;

    // 🔹 추가: 숨김 여부 (soft delete)
    @Setter
    @Builder.Default
    private boolean visible = true;
}
