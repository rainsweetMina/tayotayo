package kroryi.bus2.dto.notice;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNoticeRequestDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
