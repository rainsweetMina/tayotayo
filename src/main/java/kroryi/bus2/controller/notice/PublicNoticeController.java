package kroryi.bus2.controller.notice;


import kroryi.bus2.service.admin.notice.NoticeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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


}
