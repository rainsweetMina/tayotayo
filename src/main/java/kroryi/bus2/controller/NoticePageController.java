package kroryi.bus2.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ds")
public class NoticePageController {

    @GetMapping("/notices")
    public String noticePage() {
        return "admin/notice";  // HTML 페이지 반환
    }
}


