package kroryi.bus2.controller.admin.notice;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.service.admin.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "유저-공지사항", description = "")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicNoticeApiController {

    private final NoticeService noticeService;

    @Operation ( summary = "유저 공지 전체 목록" )
    @GetMapping("/notices")
    public List<NoticeResponseDTO> getAllNotices() {

        return noticeService.getAllNotices();
    }

}
