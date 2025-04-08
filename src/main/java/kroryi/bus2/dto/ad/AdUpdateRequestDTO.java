package kroryi.bus2.dto.ad;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdUpdateRequestDTO {
    private String title;
    private String imageUrl;
    private String linkUrl;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long companyId; // 광고회사 ID (선택)

}

