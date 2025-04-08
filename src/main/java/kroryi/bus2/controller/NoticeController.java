package kroryi.bus2.controller;

import kroryi.bus2.entity.Notice;
import kroryi.bus2.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ds/api")
@RequiredArgsConstructor
@Log4j2
public class NoticeController {

    public final NoticeService noticeService;

    // 공지사항 목록 조회
    @GetMapping("/notices")
    public String getNotices(Model model) {
        List<Notice> notices = noticeService.getAllNotices();
        model.addAttribute("notices", notices);
        return "admin/notice";
    }


    @PostMapping("/notices")
    public ResponseEntity<Notice> addNotice(@RequestBody Notice notice) {
        Notice savedNotice = noticeService.addNotice(notice);
        return ResponseEntity.ok(savedNotice);
    }

    @DeleteMapping("/notices/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }

}
