package kroryi.bus2.controller.admin;

import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public String userList(@RequestParam(required = false) String keyword, Model model) {
        List<User> users = (keyword == null || keyword.isBlank())
                ? adminUserService.getAllUsers()
                : adminUserService.searchUsers(keyword);

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        return "admin/user-list";
    }

    @PostMapping("/users/{userId}/role")
    public String changeUserRole(@PathVariable String userId,
                                 @RequestParam Role role,
                                 RedirectAttributes redirectAttributes) {
        try {
            adminUserService.changeUserRole(userId, role);
            redirectAttributes.addFlashAttribute("message", userId +"의 권한이 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
