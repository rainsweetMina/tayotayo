package kroryi.bus2.dto.lost;

import kroryi.bus2.entity.lost.LostItem;
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

    public static LostItemListResponseDTO fromEntity(LostItem item) {
        return LostItemListResponseDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .content(item.getContent())
                .busCompany(item.getBusCompany())
                .busNumber(item.getBusNumber())
                .lostTime(item.getLostTime())
                .build();
    }

}

