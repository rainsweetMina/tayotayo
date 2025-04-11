package kroryi.bus2.dto;


import kroryi.bus2.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDTO {

    private String title;
    private String author;
    private String content;

    public NoticeDTO(Notice notice) {
        this.title = notice.getTitle();
        this.author = notice.getAuthor();
        this.content = notice.getContent();
    }

}
