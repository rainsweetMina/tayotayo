package kroryi.bus2.controller.api;

import jakarta.validation.Valid;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.service.NoticeService;
import kroryi.bus2.service.NoticeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@Log4j2
public class AdminNoticeApiController {

    private final NoticeService noticeService;



    // 공지 전체 목록
    @GetMapping
    public ResponseEntity<List<NoticeResponseDTO>> getAllNotice() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    // 공지 등록
    @PostMapping
    public ResponseEntity<NoticeResponseDTO> createNotice(@RequestBody @Valid CreateNoticeRequestDTO dto) {
        log.info("📨 공지 등록 요청: {}", dto);
        NoticeResponseDTO created = noticeService.createNotice(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    // 공지 수정
    @PutMapping("/{id}")
    public ResponseEntity<NoticeResponseDTO> editNotice(@PathVariable Long id, @RequestBody @Valid UpdateNoticeRequestDTO dto) {
        NoticeResponseDTO updated = noticeService.updateNotice(id, dto);
        return ResponseEntity.ok(updated);
    }

    // 공지 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }



}
