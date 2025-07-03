package kroryi.bus2.dto.lowfloorbus;

import kroryi.bus2.entity.LowFloorBus;
import kroryi.bus2.entity.LowFloorBusFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowFloorBusResponseDTO {

    private Long id;
    private String title;
    private String author;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long viewCount;
    private boolean topNotice;
    private List<FileDTO> files;

    @Data
    @NoArgsConstructor
    public static class FileDTO {
        private Long id;
        private String originalName;
        private String storedName;
        private String fileType;
        private long fileSize;

        public static FileDTO from(LowFloorBusFile file) {
            FileDTO dto = new FileDTO();
            dto.setId(file.getId());
            dto.setOriginalName(file.getOriginalName());
            dto.setStoredName(file.getStoredName());
            dto.setFileType(file.getFileType());
            dto.setFileSize(file.getFileSize());
            return dto;
        }
    }

    public LowFloorBusResponseDTO(LowFloorBus lowFloorBus) {
        this.id = lowFloorBus.getId();
        this.title = lowFloorBus.getTitle();
        this.author = lowFloorBus.getAuthor();
        this.content = lowFloorBus.getContent();
        this.createdDate = toLocalDateTimeSafe(lowFloorBus.getCreatedDate());
        this.updatedDate = toLocalDateTimeSafe(lowFloorBus.getUpdatedDate());
        this.viewCount = lowFloorBus.getViewCount();
        this.topNotice = lowFloorBus.isTopNotice();
        this.files = lowFloorBus.getFiles().stream()
                .map(FileDTO::from)
                .collect(Collectors.toList());
    }

    private LocalDateTime toLocalDateTimeSafe(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
} 