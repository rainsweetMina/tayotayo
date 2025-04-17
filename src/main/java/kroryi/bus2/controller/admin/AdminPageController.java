package kroryi.bus2.controller.admin;

import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.service.lost.FoundItemServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final FoundItemServiceImpl foundItemServiceImpl;

    public AdminPageController(FoundItemServiceImpl foundItemServiceImpl) {
        this.foundItemServiceImpl = foundItemServiceImpl;
    }
    // ✅ 광고 관리 페이지
    @GetMapping("/ad-manage")
    public String adManagePage() {
        return "admin/adManage"; // templates/admin/adManage.html
    }

    // 추후에 더 추가 가능

    @GetMapping("/notice")
    public String noticePage() {
        return "admin/notice"; // templates/admin/notice.h
    }


    // ✅ 습득물 등록/목록/매칭 관리 페이지
    @GetMapping("/found")
    public String foundPage(Model model) {
        model.addAttribute("foundItems", foundItemServiceImpl.getAllFoundItems());
        model.addAttribute("foundItemForm", new FoundItemRequestDTO());
        return "admin/found-list";
    }
    // ✅ qna 관리 페이지
    @GetMapping("/qna")
    public String qnaManagePage() {
        return "admin/qna"; // templates/admin/qna.html
    }




}
