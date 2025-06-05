package kroryi.bus2.controller.admin.notice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.service.admin.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(noticeService.getNoticeById(id));
    }
}
