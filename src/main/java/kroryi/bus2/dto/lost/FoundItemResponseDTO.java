package kroryi.bus2.dto.lost;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FoundItemResponseDTO {
    private Long id;
    private String itemName;
    private String busCompany;
    private LocalDateTime foundTime;
    private String photoUrl;  // 🔹 사진 경로 추가
}


