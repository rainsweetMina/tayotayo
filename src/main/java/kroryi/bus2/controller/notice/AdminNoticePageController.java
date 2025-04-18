package kroryi.bus2.controller.notice;


import jakarta.validation.Valid;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;

import kroryi.bus2.service.admin.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Log4j2
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
    @PostMapping(value = "/api/admin/notices/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeResponseDTO> updateNotice(
            @PathVariable Long id,
            @RequestPart("notice") @Valid UpdateNoticeRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok(noticeService.updateNotice(id, dto, files));
    }




//    @PostMapping(value = "/api/admin/notices/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<NoticeResponseDTO> updateNotice(
//            @PathVariable Long id,
//            @RequestPart("notice") @Valid UpdateNoticeRequestDTO dto,
//            @RequestPart(value = "files", required = false) List<MultipartFile> files
//    ) {
//        return ResponseEntity.ok(noticeService.updateNotice(id, dto, files));
//    }



}
