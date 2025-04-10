package kroryi.bus2.dto.qna;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaUpdateDTO {
    private Long memberId;
    private String title;
    private String content;
    private Boolean isSecret;
}

