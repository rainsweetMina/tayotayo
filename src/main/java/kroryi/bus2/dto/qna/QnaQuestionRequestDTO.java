package kroryi.bus2.dto.qna;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QnaQuestionRequestDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @JsonProperty("isSecret")
    private boolean isSecret;

    private String userId;

    public void setUserId(String userId) {
        this.userId = userId; // 👈 실제로 값 할당
    }

    public String getUserId() {
        return userId;
    }
}
