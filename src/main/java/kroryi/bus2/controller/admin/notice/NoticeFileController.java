package kroryi.bus2.controller.admin.notice;

import kroryi.bus2.entity.Notice;
import kroryi.bus2.entity.NoticeFile;
import kroryi.bus2.service.admin.notice.NoticeService;
import kroryi.bus2.service.admin.notice.FileStorageService;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@Log4j2
public class NoticeFileController {

    private final NoticeService noticeService;
    private final FileStorageService fileStorageService;

    @Value("${file.upload.dir:uploads/notices/}")
    private String uploadDir;

    /**
     * 공지사항 파일 다운로드 (첫 번째 파일)
     * @param noticeId 공지사항 ID
     * @return 파일 리소스
     */
    @GetMapping("/{noticeId}/download")
    public ResponseEntity<Resource> downloadNoticeFile(@PathVariable Long noticeId) {
        try {
            // 1. 공지사항 조회
            Notice notice = noticeService.findById(noticeId);
            if (notice == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. 파일 정보 확인
            List<NoticeFile> files = notice.getFiles();
            if (files == null || files.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 첫 번째 파일을 다운로드
            NoticeFile file = files.get(0);
            
            // 3. 파일 리소스 로드 - 실제 저장 경로에 맞게 수정
            String uploadPath = System.getProperty("user.dir") + "/" + uploadDir + file.getStoredName();
            Path filePath = Paths.get(uploadPath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("파일을 찾을 수 없거나 읽을 수 없습니다: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 4. 파일 MIME 타입 결정
            String contentType = determineContentType(file.getOriginalName());

            // 5. 응답 헤더 설정
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

    /**
     * 여러 파일 중 특정 파일 다운로드
     */
    @GetMapping("/{noticeId}/files/{fileIndex}")
    public ResponseEntity<Resource> downloadNoticeFileByIndex(
            @PathVariable Long noticeId,
            @PathVariable int fileIndex) {

        try {
            Notice notice = noticeService.findById(noticeId);
            if (notice == null) {
                return ResponseEntity.notFound().build();
            }

            // files가 List<NoticeFile> 형태인 경우
            List<NoticeFile> files = notice.getFiles();
            if (files == null || fileIndex >= files.size()) {
                return ResponseEntity.notFound().build();
            }

            NoticeFile file = files.get(fileIndex);
            
            // 실제 저장 경로에 맞게 수정
            String uploadPath = System.getProperty("user.dir") + "/" + uploadDir + file.getStoredName();
            Path filePath = Paths.get(uploadPath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("파일을 찾을 수 없거나 읽을 수 없습니다: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(file.getOriginalName());

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
     * 파일 업로드
     */
    @PostMapping("/{noticeId}/files")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long noticeId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어있습니다.");
            }

            // 공지사항 조회
            Notice notice = noticeService.findById(noticeId);
            if (notice == null) {
                return ResponseEntity.notFound().build();
            }

            // FileStorageService를 통해 파일 저장
            List<NoticeFile> savedFiles = fileStorageService.storeFiles(List.of(file), notice);
            
            if (savedFiles.isEmpty()) {
                return ResponseEntity.internalServerError().body("파일 저장에 실패했습니다.");
            }

            // 공지사항에 파일 추가
            notice.addFile(savedFiles.get(0));
            noticeService.updateNotice(noticeId, 
                new UpdateNoticeRequestDTO(
                    notice.getTitle(), 
                    notice.getContent(), 
                    notice.isShowPopup(),
                    notice.getPopupStart(),
                    notice.getPopupEnd()
                ), null);

            return ResponseEntity.ok(savedFiles.get(0));

        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("파일 업로드 중 오류가 발생했습니다.");
        }
    }
}