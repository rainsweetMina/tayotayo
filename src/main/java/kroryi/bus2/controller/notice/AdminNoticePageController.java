package kroryi.bus2.controller.notice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;





@Controller
@RequestMapping("/admin")
public class AdminNoticePageController {

    @GetMapping("/notices")
    public String noticePage() {
        return "admin/notice"; // templates/admin/notice.html 이라고 가정
    }



}
