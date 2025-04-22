package kroryi.bus2.dto.qna;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QnaQuestionRequestDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @JsonProperty("isSecret")
    private boolean isSecret;
}
