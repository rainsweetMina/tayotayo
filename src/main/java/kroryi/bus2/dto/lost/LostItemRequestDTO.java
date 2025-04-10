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
public class LostItemRequestDTO {
    private String title;
    private String content;
    private String busNumber;
    private LocalDateTime lostTime;
    private Long reporterId; // 일반회원의 user.id

}

