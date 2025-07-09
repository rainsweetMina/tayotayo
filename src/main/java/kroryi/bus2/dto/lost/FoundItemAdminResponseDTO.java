package kroryi.bus2.dto.lost;

import kroryi.bus2.entity.lost.FoundItem;
import kroryi.bus2.entity.lost.FoundStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoundItemAdminResponseDTO {
    private Long id;
    private String itemName;
    private String busCompany;
    private String busNumber;
    private String foundPlace;
    private LocalDate foundTime;
    private String content;
    private String storageLocation;
    private String handlerContact;
    private String handlerEmail;
    private FoundStatus status;
    //    private boolean isHidden;
    private boolean visible;
    private boolean isDeleted;
    private String photoUrl;
    @Getter
    private Long handlerId;
    @Getter
    @Setter
    private Boolean matched;
    private String statusName;


    public static FoundItemAdminResponseDTO fromEntity(FoundItem entity) {

        return FoundItemAdminResponseDTO.builder()
                .id(entity.getId())
                .itemName(entity.getItemName())
                .busCompany(entity.getBusCompany())
                .busNumber(entity.getBusNumber())
                .foundPlace(entity.getFoundPlace())
                .foundTime(entity.getFoundTime() != null ? entity.getFoundTime().toLocalDate() : null)
                .content(entity.getContent())
                .storageLocation(entity.getStorageLocation())
                .handlerContact(entity.getHandlerContact())
                .handlerEmail(entity.getHandlerEmail())
                .status(entity.getStatus())
                .statusName(entity.getStatus() != null ? entity.getStatus().getDisplayName() : null) // null 체크 추가!
                .photoUrl(entity.getPhoto() != null ? entity.getPhoto().getUrl() : null)
//                .isHidden(entity.isHidden())
                .visible(entity.isVisible())      // ← 여기도 추가!
                .isDeleted(entity.getIsDeleted())
                .handlerId(entity.getHandler() != null ? entity.getHandler().getId() : null)
                .matched(entity.isMatched())
                .build();

    }

}
