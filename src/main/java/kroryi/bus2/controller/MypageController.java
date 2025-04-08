package kroryi.bus2.controller.mypage;

import jakarta.validation.Valid;
import kroryi.bus2.dto.mypage.ChangePasswordDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Log4j2
@Controller
@RequiredArgsConstructor
public class MypageController {

    private final UserService userService;

    // 마이페이지
    @GetMapping("/mypage")
    public String myPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        if (userId == null) return "redirect:/login";

        User user = userService.findByUserId(userId);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "mypage/mypage";
    }

    // 비밀번호 변경 폼
    @GetMapping("/mypage/password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        return "mypage/password";
    }

    // 비밀번호 변경 처리
    @PostMapping("/mypage/password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordDTO dto, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        if (!dto.getModifyPassword().equals(dto.getModifyPasswordCheck())) {
            model.addAttribute("error", "새 비밀번호가 일치하지 않습니다.");
            return "mypage/password";
        }

        try {
            boolean success = userService.changePassword(userId, dto.getCurrentPassword(), dto.getModifyPassword());
            if (!success) {
                model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
                return "mypage/password";
            }

            model.addAttribute("success", "비밀번호가 성공적으로 변경되었습니다.");
            return "mypage/password";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "mypage/password";
        }
    }

    // 회원정보 수정 폼
    @GetMapping("/mypage/modify")
    public String showModifyForm(Model model) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUserId(userId);

        ModifyUserDTO dto = new ModifyUserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setSignupType(user.getSignupType());
        dto.setSignupDate(user.getSignupDate());
        dto.setRole(user.getRole());

        model.addAttribute("modifyUserDTO", dto);
        return "mypage/modify";
    }

    // 회원정보 수정 처리
    @PostMapping("/mypage/modify")
    public String modifyUser(@Valid @ModelAttribute ModifyUserDTO dto,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            boolean success = userService.modifyUserInfo(userId, dto);
            if (!success) {
                model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
                return "mypage/modify";
            }

            // 성공 메시지를 flash 속성으로 전달
            redirectAttributes.addFlashAttribute("success", "회원 정보가 성공적으로 수정되었습니다.");
            return "redirect:/mypage";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "mypage/modify";
        }
    }
}
