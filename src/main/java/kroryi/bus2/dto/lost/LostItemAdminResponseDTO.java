package kroryi.bus2.dto.lost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class LostItemAdminResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String busNumber;
    private String busCompany;
    private LocalDateTime lostTime;
    private boolean matched;
    private boolean visible;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long memberId;
}
