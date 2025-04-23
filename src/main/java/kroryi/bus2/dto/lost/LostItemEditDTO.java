package kroryi.bus2.dto.lost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostItemEditDTO {
    private Long id;
    private String title;
    private String content;
    private String busNumber;
    private String busCompany;
    private LocalDateTime lostTime;
}

