package kroryi.bus2.dto.lost;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FoundItemAdminResponseDTO {
    private Long id;
    private String itemName;
    private String busCompany;
    private String busNumber;
    private String foundPlace;
    private String content;
    private String handlerContact;
    private String handlerEmail;
    private String status;
    private String storageLocation;
    private LocalDateTime foundTime;
    private String photoUrl;
    private boolean deleted;
    private boolean visible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long handlerId;
}