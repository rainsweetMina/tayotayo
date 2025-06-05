package kroryi.bus2.dto.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공지사항 등록 요청 DTO")
public class CreateNoticeRequestDTO {

    @Schema(description = "공지 제목", example = "시스템 점검 안내")
    @NotBlank
    private String title;

    @Schema(description = "작성자", example = "관리자")
    @NotBlank
    private String author;

    @Schema(description = "공지 내용", example = "시스템 점검으로 인해...")
    @NotBlank
    private String content;

    @Schema(description = "팝업 노출 여부", example = "true")
    private boolean showPopup;

    @Schema(description = "팝업 시작일시", example = "2025-06-05T08:00:00")
    private LocalDateTime popupStart;

    @Schema(description = "팝업 종료일시", example = "2025-06-06T08:00:00")
    private LocalDateTime popupEnd;
}
