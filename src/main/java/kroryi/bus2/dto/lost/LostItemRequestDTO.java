package kroryi.bus2.dto.lost;

import jakarta.validation.constraints.NotNull;
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
    private String busCompany;
    @NotNull(message = "분실일은 필수입니다.")
    private LocalDateTime lostTime;
    private Long reporterId; // 일반회원의 user.id

}

