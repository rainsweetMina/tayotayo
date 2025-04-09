package kroryi.bus2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NoticePageController {

    @GetMapping("/notice")
    public String noticePage() {
        return "forward:/notice.html"; // static 폴더에 있는 notice.html
    }
}
