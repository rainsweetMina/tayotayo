package kroryi.bus2.dto.lost;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class FoundItemRequestDTO {
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
    private Long handlerId;
}

