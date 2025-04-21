package kroryi.bus2.dto.qna;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaListDTO {
    private Long id;
    private String title;
    private String status;
    private String username;
    private boolean isSecret;
    private LocalDateTime createdAt;
}
