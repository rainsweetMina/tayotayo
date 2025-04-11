package kroryi.bus2.controller.api;

import kroryi.bus2.dto.NoticeDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Log4j2
public class AdminNoticeApiController {

    public final NoticeService noticeService;


    // 공지 전체 목록
    @GetMapping("/notices")
    public ResponseEntity<List<Notice>> getAllNotice() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    // 공지 등록
    @PostMapping("/notices")
    public ResponseEntity<NoticeDTO> addNotice(@RequestBody NoticeDTO dto) {
        log.info("공지 등록 요청--->: {}", dto);

        return ResponseEntity.ok(noticeService.addNotice(dto));
    }

    // 공지 삭제
    @DeleteMapping("/notices/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }

    // 공지 수정
    @PutMapping("/notices/{id}")
    public ResponseEntity<Notice> updateNotice(@PathVariable Long id, @RequestBody NoticeDTO dto) {
        Notice updated = noticeService.updateNotice(id, dto);
        return ResponseEntity.ok(updated);
    }


}
