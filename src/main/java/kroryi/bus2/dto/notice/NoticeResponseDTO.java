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

    public NoticeResponseDTO(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.author = notice.getAuthor();
        this.content = notice.getContent();
        this.createdDate = toLocalDateTimeSafe(notice.getCreatedDate());
        this.updatedDate = toLocalDateTimeSafe(notice.getUpdatedDate());
    }

    private LocalDateTime toLocalDateTimeSafe(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
