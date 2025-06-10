package kroryi.bus2.controller.admin.notice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.service.admin.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

@Tag(name = "관리자-공지사항", description = "공지사항 관리 API")
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@Log4j2
public class AdminNoticeApiController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 전체 목록")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<NoticeResponseDTO>> getAllNotice() {
        log.info("🔴 ADMIN API 호출됨 - /api/admin/notices");
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    @Operation(summary = "공지 상세 조회")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NoticeResponseDTO> getNotice(@PathVariable Long id) {
        log.info("🔴 ADMIN API 호출됨 - /api/admin/notices/{}", id);
        NoticeResponseDTO notice = noticeService.getNoticeById(id);
        log.info("🔴 공지사항 조회 완료 - ID: {}, 제목: {}, 조회수: {}", id, notice.getTitle(), notice.getViewCount());
        return ResponseEntity.ok(notice);
    }

    @Operation(summary = "공지 등록")
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<NoticeResponseDTO> createNotice(
            @RequestPart("notice") @Valid CreateNoticeRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            log.info("📨 공지 등록 요청: {}", dto);
            
            if (files != null && !files.isEmpty()) {
                log.info("📨 첨부파일 정보: 개수={}", files.size());
                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    log.info("📨 파일[{}]: 이름={}, 크기={}, 타입={}", 
                        i, file.getOriginalFilename(), file.getSize(), file.getContentType());
                }
            } else {
                log.info("📨 첨부파일 없음");
            }
            
            NoticeResponseDTO created = noticeService.createNotice(dto, files);
            log.info("📨 공지 등록 성공: ID={}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("📨 공지 등록 실패", e);
            throw e; // 예외를 다시 던져서 글로벌 예외 핸들러가 처리할 수 있도록 함
        }
    }

    @Operation(summary = "공지 수정")
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<NoticeResponseDTO> updateNotice(
            @PathVariable Long id,
            @RequestPart(value = "notice", required = true) @Valid UpdateNoticeRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok(noticeService.updateNotice(id, dto, files));
    }

    @Operation(summary = "공지 삭제")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공지 첨부파일 다운로드")
    @GetMapping("/files/{noticeId}/{fileName}")
    public ResponseEntity<Resource> downloadNoticeFile(
            @PathVariable Long noticeId,
            @PathVariable String fileName
    ) {
        try {
            // 파일 실제 경로 구성
            Path filePath = Paths.get("uploads/notices/" + noticeId).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Content-Disposition: 파일 다운로드로 처리
            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("📛 파일 경로 오류", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("📛 파일 다운로드 실패", e);
            return ResponseEntity.internalServerError().build();
        }


    }

}
