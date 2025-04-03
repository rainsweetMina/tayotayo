package kroryi.bus2.dto.lost;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FoundItemListResponseDTO {
    private Long id;
    private String itemName;
    private String busCompany;
    private LocalDateTime foundTime;
    private boolean matched;
}
