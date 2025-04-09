package kroryi.bus2.controller;

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

    @GetMapping("/notices")
    public List<Notice> getAllNotices() {
        return noticeService.getAllNotices();
    }

    @PostMapping("/notices")
    public ResponseEntity<Notice> addNotice(@RequestBody Notice notice) {
        return ResponseEntity.ok(noticeService.addNotice(notice));
    }

    @DeleteMapping("/notices/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }

}
