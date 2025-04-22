package kroryi.bus2.dto.lost;

import kroryi.bus2.entity.lost.FoundStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FoundItemListResponseDTO {
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
}
