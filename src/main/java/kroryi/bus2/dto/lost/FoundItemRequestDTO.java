package kroryi.bus2.dto.lost;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FoundItemRequestDTO {
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
    private Long handlerId;
}
