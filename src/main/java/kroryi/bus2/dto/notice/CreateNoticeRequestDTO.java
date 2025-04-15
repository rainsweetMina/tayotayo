package kroryi.bus2.dto.notice;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoticeRequestDTO {
    // 제목, 작성자, 내용
    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotBlank
    private String content;
    //팝업관련
    private boolean showPopup;
    private LocalDateTime popupStart;
    private LocalDateTime popupEnd;

}
