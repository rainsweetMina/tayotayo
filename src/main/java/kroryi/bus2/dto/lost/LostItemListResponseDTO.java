package kroryi.bus2.dto.lost;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LostItemListResponseDTO {
    private Long id;
    private String title;
    private String busNumber;
    private LocalDateTime lostTime;
    private boolean matched;
}
