package kroryi.bus2.controller.notice;


import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;

import kroryi.bus2.service.admin.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminNoticePageController {

    private final NoticeService noticeService;

    // 등록
    @PostMapping("/api/admin/notices")
    @ResponseBody
    public ResponseEntity<Void> createNotice(@RequestBody CreateNoticeRequestDTO dto) {
        noticeService.createNotice(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 수정
    @PutMapping("/api/admin/notices/{id}")
    public ResponseEntity<Void> updateNotice(@PathVariable Long id, @RequestBody UpdateNoticeRequestDTO dto) {
        noticeService.updateNotice(id, dto);
        return ResponseEntity.noContent().build();
    }

}
