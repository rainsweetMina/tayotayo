package kroryi.bus2.dto.lost;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostItemRequestDTO {
    private String title;
    private String content;
    private String busNumber;
    private LocalDateTime lostTime;
    private Long reporterId; // 일반회원의 user.id
}

