package kroryi.bus2.dto.qna;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QnaUpdateDTO {

    private String title;
    private String content;
    private Boolean isSecret;
}
