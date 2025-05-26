package kroryi.bus2.dto.notice;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.entity.NoticeFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDTO {
    private Long id;
    private String title;
    private String author;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private boolean showPopup;
    private LocalDateTime popupStart;
    private LocalDateTime popupEnd;

    private List<FileDTO> files;


    public NoticeDTO(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.author = notice.getAuthor();
        this.content = notice.getContent();
        this.createdDate = notice.getCreatedDate().toLocalDateTime();  // Timestamp → LocalDateTime
        this.updatedDate = notice.getUpdatedDate().toLocalDateTime();
        this.showPopup = notice.isShowPopup();
        this.popupStart = notice.getPopupStart();
        this.popupEnd = notice.getPopupEnd();
        this.files = notice.getFiles().stream()
                .map(FileDTO::from)
                .collect(Collectors.toList());
    }


    @Data
    @NoArgsConstructor
    public static class FileDTO {
        private String originalName;
        private String storedName;
        private String fileType;

        public static FileDTO from(NoticeFile file) {
            FileDTO dto = new FileDTO();
            dto.setOriginalName(file.getOriginalName());
            dto.setStoredName(file.getStoredName());
            dto.setFileType(file.getFileType());
            return dto;
        }
    }
}
