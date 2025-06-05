package kroryi.bus2.dto.notice;

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

    private List<FileDTO> files;


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



    public NoticeResponseDTO(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.author = notice.getAuthor();
        this.content = notice.getContent();
        this.createdDate = toLocalDateTimeSafe(notice.getCreatedDate());
        this.updatedDate = toLocalDateTimeSafe(notice.getUpdatedDate());
        this.files = notice.getFiles().stream()
                .map(FileDTO::from)
                .collect(Collectors.toList());
        // ✅ 팝업 관련 필드 추가
        this.showPopup = notice.isShowPopup();
        this.popupStart = notice.getPopupStart();
        this.popupEnd = notice.getPopupEnd();
    }



    private LocalDateTime toLocalDateTimeSafe(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
