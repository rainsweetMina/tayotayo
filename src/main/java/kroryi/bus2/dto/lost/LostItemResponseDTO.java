package kroryi.bus2.dto.lost;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LostItemResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String busNumber;
    private LocalDateTime lostTime;
    private Long memberId; // User id만 사용
    private boolean matched;
    private boolean visible;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

