package kroryi.bus2.controller.admin.notice;


import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.notice.NoticeResponseDTO;
import kroryi.bus2.entity.Notice;
import kroryi.bus2.service.admin.notice.NoticeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;


@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
// 관리자 외 유저 공지
public class PublicNoticeController {

    private final NoticeServiceImpl noticeService;

    @GetMapping
    public String getNoticePage(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        return "/public/notice";
    }
    //팝업관련
    @GetMapping("/popup")
    public ResponseEntity<NoticeResponseDTO> getPopupNotice() {
        Optional<Notice> popup = noticeService.findValidPopup();
        return popup.map(notice -> ResponseEntity.ok(new NoticeResponseDTO(notice)))
                .orElse(ResponseEntity.noContent().build());
    }



}
