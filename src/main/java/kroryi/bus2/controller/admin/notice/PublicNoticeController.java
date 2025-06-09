package kroryi.bus2.controller.admin.notice;


import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.entity.NoticeFile;
import kroryi.bus2.service.admin.notice.NoticeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
// 관리자 외 유저 공지
public class PublicNoticeController {

    private final NoticeServiceImpl noticeService;
    private static final Logger log = LoggerFactory.getLogger(PublicNoticeController.class);

    @GetMapping
    public String getNoticePage(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        return "/public/notice";
    }
    //팝업관련
    @GetMapping("/popup")
    public ResponseEntity<NoticeResponseDTO> getPopupNotice() {
        Optional<Notice> popup = noticeService.findValidPopup();
        return popup.map(notice -> ResponseEntity.ok(new NoticeResponseDTO(notice)))
                .orElse(ResponseEntity.noContent().build());
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
            
            // 파일 경로 구성 - 관리자와 동일한 저장소 사용
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
    @GetMapping("/{noticeId}/files/{fileIndex}")
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

    /**
     * 에디터에 삽입된 이미지 가져오기
     */
    @GetMapping("/notices/images/{filename}")
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
