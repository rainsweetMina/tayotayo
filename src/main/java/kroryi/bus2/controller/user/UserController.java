package kroryi.bus2.controller.user;

import kroryi.bus2.dto.user.JoinRequestDTO;
import kroryi.bus2.dto.user.LoginFormDTO;
import kroryi.bus2.service.user.EmailService;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    // 로그인 페이지
    @GetMapping("/login")
    public String login(@RequestParam(value = "errorCode", required = false) String errorCode,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        log.info("로그인 페이지 요청");
        log.info("logout: {}", logout);

        if (logout != null) {
            model.addAttribute("logoutMessage", "로그아웃되었습니다.");
        }

        if (errorCode != null) {
            String errorMessage = switch (errorCode) {
                case "bad_credentials" -> "아이디 또는 비밀번호가 올바르지 않습니다.";
                case "disabled"       -> "비활성화된 계정입니다.";
                case "locked"         -> "잠긴 계정입니다.";
                case "expired"        -> "계정이 만료되었습니다.";
                default               -> "로그인 중 오류가 발생했습니다.";
            };
            model.addAttribute("errorMessage", errorMessage);
        }

        model.addAttribute("loginForm", new LoginFormDTO());
        return "user/login";
    }

    // 회원가입 페이지
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("joinRequestDTO")) {
            model.addAttribute("joinRequestDTO", new JoinRequestDTO());
        }
        return "user/register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String register(@ModelAttribute("joinRequestDTO") JoinRequestDTO jdto,
                           RedirectAttributes redirectAttributes) {
        try {
            if (!jdto.getEmailVerified()) {
                throw new IllegalArgumentException("이메일 인증을 완료해주세요.");
            }

            userService.join(jdto);
            return "redirect:/login?registerSuccess=true";

        } catch (Exception e) {
            log.error("회원가입 오류: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("registrationSuccess", false);
            redirectAttributes.addFlashAttribute("joinRequestDTO", jdto);
            return "redirect:/register";
        }
    }

    // 이메일 인증 코드 전송
    @ResponseBody
    @GetMapping("/email/send")
    public String sendEmailVerificationCode(@RequestParam String email) {
        emailService.sendVerificationCode(email);
        return "인증 코드 전송 완료";
    }

    // 이메일 인증 코드 검증
    @ResponseBody
    @GetMapping("/email/verify")
    public Map<String, Object> verifyEmailCode(@RequestParam String email,
                                               @RequestParam String code) {
        boolean result = emailService.verifyCode(email, code);
        Map<String, Object> response = new HashMap<>();
        response.put("verified", result);
        return response;
    }
}
