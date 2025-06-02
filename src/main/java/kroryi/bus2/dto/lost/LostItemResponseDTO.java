package kroryi.bus2.dto.lost;

import kroryi.bus2.entity.lost.LostItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LostItemResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String busNumber;
    private String busCompany;
    private LocalDateTime lostTime;
    private Long memberId; // User id만 사용
    private boolean matched;
    private boolean visible;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LostItemResponseDTO fromEntity(LostItem item) {
        return LostItemResponseDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .content(item.getContent())
                .busCompany(item.getBusCompany())   // ✅ 누락되면 목록에서 회사명 안 뜸
                .busNumber(item.getBusNumber())     // ✅ 노선번호도 함께
                .lostTime(item.getLostTime())
                .memberId(item.getReporter() != null ? item.getReporter().getId() : null) // null 체크
                .matched(item.isMatched())
                .visible(item.isVisible())
                .deleted(item.isDeleted())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

}

