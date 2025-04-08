package kroryi.bus2.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping("/ad-manage")
    public String adManagePage() {
        return "admin/adManage"; // templates/admin/adManage.html
    }

    // 추후에 더 추가 가능
}
