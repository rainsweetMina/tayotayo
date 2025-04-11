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
    private boolean matched;

    private boolean visible; // ✅ 추가해야 함
    private boolean deleted; // ✅ 추가해야 함
    private LocalDateTime createdAt; // ✅ 추가해야 함
    private LocalDateTime updatedAt; // ✅ 추가해야 함
}

