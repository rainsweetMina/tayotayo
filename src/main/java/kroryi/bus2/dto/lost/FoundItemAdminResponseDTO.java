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
@ToString
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
    private boolean isHidden;
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
                .statusName(entity.getStatus().getDisplayName()) // 추가!
                .photoUrl(entity.getPhoto() != null ? entity.getPhoto().getUrl() : null)
                .isHidden(entity.isHidden())
                .isDeleted(entity.getIsDeleted())
                .handlerId(entity.getHandler() != null ? entity.getHandler().getId() : null)
                .matched(entity.isMatched())
                .build();

    }

}
