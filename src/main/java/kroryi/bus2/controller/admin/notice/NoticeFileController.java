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
import java.io.File;
import java.util.Map;
import java.util.HashMap;

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

    /**
     * 이미지 파일 직접 보기 (브라우저에서 렌더링)
     */
    @GetMapping("/{noticeId}/images/{fileIndex}")
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
     * 에디터용 이미지 업로드
     */
    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImageForEditor(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어있습니다.");
            }
            
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("이미지 파일만 업로드 가능합니다.");
            }

            // 파일 저장 경로 생성
            String uploadDirPath = System.getProperty("user.dir") + "/uploads/editor";
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    return ResponseEntity.internalServerError().body("업로드 디렉토리를 생성할 수 없습니다.");
                }
            }

            // 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID() + extension;
            
            // 파일 저장
            File targetFile = new File(uploadDirPath + File.separator + newFilename);
            file.transferTo(targetFile);

            // 이미지 URL 생성
            String imageUrl = "/api/admin/notices/images/" + newFilename;
            
            // 응답 데이터 생성
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            
            log.info("에디터 이미지 업로드 성공: {}", imageUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("에디터 이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 에디터에 삽입된 이미지 가져오기
     */
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getEditorImage(@PathVariable String filename) {
        try {
            // 파일 경로 생성
            String uploadDirPath = System.getProperty("user.dir") + "/uploads/editor";
            Path filePath = Paths.get(uploadDirPath + File.separator + filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("이미지 파일을 찾을 수 없거나 읽을 수 없습니다: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 파일 확장자에 따른 MIME 타입 설정
            String contentType = determineContentType(filename);
            
            // Content-Disposition 헤더를 설정하지 않으면 브라우저에서 바로 표시됨
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("에디터 이미지 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}