package kroryi.bus2.controller.api;


import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.service.NoticeService;
import kroryi.bus2.service.NoticeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicNoticeApiController {

    private final NoticeService noticeService;

    @GetMapping("/notices")
    public List<NoticeResponseDTO> getAllNotices() {

        return noticeService.getAllNotices();
    }

}
