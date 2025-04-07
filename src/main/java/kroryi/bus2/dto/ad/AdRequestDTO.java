package kroryi.bus2.dto.ad;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdRequestDTO {
    private String title;
    private String imageUrl;
    private String linkUrl;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long companyId;

}
