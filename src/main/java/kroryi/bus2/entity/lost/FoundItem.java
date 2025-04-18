package kroryi.bus2.entity.lost;

import jakarta.persistence.*;
import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.entity.user.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoundItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String busCompany;
    private String busNumber;
    private String foundPlace;
    private LocalDateTime foundTime;
    private String content;
    private String storageLocation;
    private String handlerContact;
    private String handlerEmail;

    @Enumerated(EnumType.STRING)
    private FoundStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id")
    private User handler;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden = false;


    @Builder.Default
    @Column(nullable = false)
    private boolean visible = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean matched = false;

    @OneToOne(mappedBy = "foundItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Photo photo;

    // soft delete
    public void markDeleted() {
        this.isDeleted = true;
    }

    public void markHidden() {
        this.isHidden = true;
    }

    public void matchAndComplete() {
        this.status = FoundStatus.RETURNED;
    }

    public void update(FoundItemRequestDTO dto) {
        this.itemName = dto.getItemName();
        this.busCompany = dto.getBusCompany();
        this.busNumber = dto.getBusNumber();
        this.foundPlace = dto.getFoundPlace();
        this.foundTime = dto.getFoundTime().atStartOfDay();
        this.content = dto.getContent();
        this.handlerContact = dto.getHandlerContact();
        this.handlerEmail = dto.getHandlerEmail();
        this.status = dto.getStatus();
        this.storageLocation = dto.getStorageLocation();

        // 🔥 photoUrl은 setter로만 직접 관리 (이미지 처리 쪽에서만 설정)

        if (dto.getStatus() == FoundStatus.RETURNED && !this.matched) {
            this.matched = true;
        }
    }

}
