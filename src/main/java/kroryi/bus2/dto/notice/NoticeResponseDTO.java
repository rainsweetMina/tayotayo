package kroryi.bus2.dto.notice;

import kroryi.bus2.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponseDTO {

    private Long id;
    private String title;
    private String author;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    //팝업관련
    private boolean showPopup;
    private LocalDateTime popupStart;
    private LocalDateTime popupEnd;


    public NoticeResponseDTO(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.author = notice.getAuthor();
        this.content = notice.getContent();
        this.createdDate = toLocalDateTimeSafe(notice.getCreatedDate());
        this.updatedDate = toLocalDateTimeSafe(notice.getUpdatedDate());
        // ✅ 팝업 관련 필드 추가
        this.showPopup = notice.isShowPopup();
        this.popupStart = notice.getPopupStart();
        this.popupEnd = notice.getPopupEnd();
    }

    private LocalDateTime toLocalDateTimeSafe(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
