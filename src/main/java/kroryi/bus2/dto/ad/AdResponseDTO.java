package kroryi.bus2.dto.ad;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdResponseDTO {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String status;

    private String companyName;
    private String managerName;
    private String contactNumber;
    private String email;
}