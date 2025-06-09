package kroryi.bus2.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    @RequestMapping(value = {
            "/mypage/**",
            "/admin/**",
            "/bus/**",
            "/lost/**",
            "/qna/**"
    })
    public String forward() {
        return "forward:/index.html"; // Vue SPA 진입점
    }
}
