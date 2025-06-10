package kroryi.bus2.controller.admin.notice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.entity.NoticeFile;
import kroryi.bus2.service.admin.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Tag(name = "유저-공지사항", description = "공개 공지사항 조회 API")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicNoticeApiController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 전체 목록")
    @GetMapping("/notices")
    public ResponseEntity<List<NoticeResponseDTO>> getAllNotices() {
        log.info("🔵 PUBLIC API 호출됨 - /api/public/notices");
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    @Operation(summary = "공지사항 상세 조회")
    @GetMapping("/notices/{id}")
    public ResponseEntity<NoticeResponseDTO> getNotice(@PathVariable Long id) {
        log.info("🔵 PUBLIC API 호출됨 - /api/public/notices/{}", id);
        NoticeResponseDTO notice = noticeService.getNoticeById(id);
        log.info("🔵 공지사항 조회 완료 - ID: {}, 제목: {}, 조회수: {}", id, notice.getTitle(), notice.getViewCount());
        return ResponseEntity.ok(notice);
    }
    
    /**
     * 이미지 파일 직접 보기 (브라우저에서 렌더링)
     */
    @Operation(summary = "공지사항 이미지 조회")
    @GetMapping("/notices/{noticeId}/images/{fileIndex}")
    public ResponseEntity<Resource> viewNoticeImage(
            @PathVariable Long noticeId,
            @PathVariable int fileIndex) {

        try {
            Notice notice = noticeService.findById(noticeId);
            if (notice == null) {
                return ResponseEntity.notFound().build();
            }

            List<NoticeFile> files = notice.getFiles();
            if (files == null || fileIndex >= files.size()) {
                return ResponseEntity.notFound().build();
            }

            NoticeFile file = files.get(fileIndex);
            
            // 이미지 파일인지 확인
            String fileType = file.getFileType();
            if (fileType == null || !fileType.startsWith("image/")) {
                log.warn("이미지 파일이 아닙니다: {}, {}", fileType, file.getOriginalName());
                return ResponseEntity.badRequest().body(null);
            }
            
            // 파일 경로 구성
            String uploadDir = "uploads/notices/";
            String uploadPath = System.getProperty("user.dir") + "/" + uploadDir + file.getStoredName();
            Path filePath = Paths.get(uploadPath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("파일을 찾을 수 없거나 읽을 수 없습니다: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 이미지 파일은 Content-Disposition 헤더를 설정하지 않음 (브라우저에서 바로 표시)
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileType))
                    .body(resource);

        } catch (Exception e) {
            log.error("이미지 파일 표시 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 첨부파일 다운로드
     */
    @Operation(summary = "공지사항 첨부파일 다운로드")
    @GetMapping("/notices/{noticeId}/files/{fileIndex}")
    public ResponseEntity<Resource> downloadNoticeFile(
            @PathVariable Long noticeId,
            @PathVariable int fileIndex) {

        try {
            Notice notice = noticeService.findById(noticeId);
            if (notice == null) {
                return ResponseEntity.notFound().build();
            }

            List<NoticeFile> files = notice.getFiles();
            if (files == null || fileIndex >= files.size()) {
                return ResponseEntity.notFound().build();
            }

            NoticeFile file = files.get(fileIndex);
            
            // 파일 경로 구성
            String uploadDir = "uploads/notices/";
            String uploadPath = System.getProperty("user.dir") + "/" + uploadDir + file.getStoredName();
            Path filePath = Paths.get(uploadPath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("파일을 찾을 수 없거나 읽을 수 없습니다: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 파일 MIME 타입 결정
            String contentType = determineContentType(file.getOriginalName());

            // 다운로드를 위한 헤더 설정
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getOriginalName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("파일 다운로드 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 파일 MIME 타입 결정
     */
    private String determineContentType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "txt":
                return "text/plain";
            case "zip":
                return "application/zip";
            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}
