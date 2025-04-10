package kroryi.bus2.controller;

import kroryi.bus2.dto.NoticeDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ds/api")
@RequiredArgsConstructor
@Log4j2
public class NoticeController {

    public final NoticeService noticeService;


    // 공지 전체 목록
    @GetMapping("/notices")
    public ResponseEntity<List<Notice>> addAllNotice() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    // 공지 등록
    @PostMapping("/notices")
    public ResponseEntity<Notice> addNotice(@RequestBody NoticeDTO dto) {
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
