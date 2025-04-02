package kroryi.bus2.dto.lost;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FoundItemRequestDTO {
    private String itemName;
    private String busCompany;
    private LocalDateTime foundTime;
    private Long handlerId; // 버스회사 관리자 user.id
}
