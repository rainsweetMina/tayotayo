package kroryi.bus2.dto.lost;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class LostItemListResponseDTO {
    private Long id;
    private String title;
    private String busNumber;
    private LocalDateTime lostTime;
    private String content;
    private String busCompany;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

