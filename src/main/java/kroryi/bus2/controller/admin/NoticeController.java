package kroryi.bus2.controller.admin;

import kroryi.bus2.dto.notice.NoticeDTO;
import kroryi.bus2.service.admin.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<List<NoticeDTO>> getAllNotices() {
        return ResponseEntity.ok(noticeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeDTO> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<NoticeDTO> createNotice(
            @ModelAttribute NoticeDTO dto,
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(noticeService.createNotice(dto, files));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoticeDTO> updateNotice(
            @PathVariable Long id,
            @ModelAttribute NoticeDTO dto,
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(noticeService.updateNotice(id, dto, files));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id) {
        noticeService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
