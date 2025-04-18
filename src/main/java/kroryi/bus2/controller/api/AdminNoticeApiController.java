package kroryi.bus2.controller.api;

import jakarta.validation.Valid;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.dto.notice.CreateNoticeRequestDTO;
import kroryi.bus2.dto.notice.UpdateNoticeRequestDTO;
import kroryi.bus2.service.admin.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<NoticeResponseDTO> createNotice(
            @RequestPart("notice") @Valid CreateNoticeRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        log.info("📨 공지 등록 요청: {}", dto);
        NoticeResponseDTO created = noticeService.createNotice(dto, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }



    // 공지 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeResponseDTO> updateNotice(
            @PathVariable Long id,
            @RequestPart("notice") @Valid UpdateNoticeRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok(noticeService.updateNotice(id, dto, files));
    }



    // 공지 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }



}
