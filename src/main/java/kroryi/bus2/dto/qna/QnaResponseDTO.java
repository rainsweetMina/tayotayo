package kroryi.bus2.dto.qna;

import kroryi.bus2.entity.QnaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaResponseDTO {

    private Long id;
    private Long memberId;
    private String title;
    private String content;

    private QnaStatus status;
    private String answer;

    private boolean isSecret;
    private boolean visible;
    private boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

