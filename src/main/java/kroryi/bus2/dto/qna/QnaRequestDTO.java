package kroryi.bus2.dto.qna;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaRequestDTO {

    @NotNull
    private Long memberId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private boolean isSecret; // 비공개 여부
}

