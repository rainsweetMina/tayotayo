package kroryi.bus2.controller;

import jakarta.validation.Valid;
import kroryi.bus2.dto.user.JoinRequestDTO;
import kroryi.bus2.dto.user.LoginRequestDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 👉 회원가입 폼
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("joinRequestDTO", new JoinRequestDTO());
        return "user/register";
    }

    // 👉 회원가입 처리
    @PostMapping("/register")
    public String register(@ModelAttribute @Valid JoinRequestDTO joinRequestDTO, BindingResult bindingResult) {
        if (!joinRequestDTO.getPassword().equals(joinRequestDTO.getPasswordCheck())) {
            bindingResult.rejectValue("passwordCheck", "password.mismatch", "비밀번호가 일치하지 않습니다.");
        }

        if (userService.checkUserIdDuplicate(joinRequestDTO.getUserId())) {
            bindingResult.rejectValue("userId", "userId.duplicate", "이미 사용 중인 아이디입니다.");
        }

        if (bindingResult.hasErrors()) {
            return "user/register";
        }

        userService.join(joinRequestDTO);
        return "redirect:/login";
    }

    // 👉 로그인 폼
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new LoginRequestDTO());
        return "user/login";
    }

    // 👉 마이페이지
    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUserId(userDetails.getUsername());
        model.addAttribute("user", user);
        return "user/mypage";
    }

    // 👉 회원 탈퇴
    @PostMapping("/mypage/delete")
    public String deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            userService.deleteByUserId(userDetails.getUsername());
        }
        return "redirect:/login?deleted";
    }
}
