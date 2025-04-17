package kroryi.bus2.controller.notice;


import jakarta.validation.Valid;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;

import kroryi.bus2.service.admin.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminNoticePageController {

    private final NoticeService noticeService;

    @PostMapping(value = "/api/admin/notices", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<Void> createNotice(@RequestPart("notice") @Valid CreateNoticeRequestDTO dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        noticeService.createNotice(dto, files);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    // 수정
    @PutMapping("/api/admin/notices/{id}")
    public ResponseEntity<Void> updateNotice(@PathVariable Long id, @RequestBody UpdateNoticeRequestDTO dto) {
        noticeService.updateNotice(id, dto);
        return ResponseEntity.noContent().build();
    }

}
