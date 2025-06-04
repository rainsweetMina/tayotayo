package kroryi.bus2.controller.user;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kroryi.bus2.dto.user.JoinRequestDTO;
import kroryi.bus2.dto.user.LoginFormDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.EmailService;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Hidden
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "redirect:/admin";
    }


    // 로그인 페이지
    @GetMapping("/auth/login")
    public String login(@RequestParam(value = "errorCode", required = false) String errorCode,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "redirect", required = false) String redirect,
                        Model model, HttpSession httpsession) {
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

        // 🔁 redirect 파라미터가 있으면 세션에 저장
        if (redirect != null && !redirect.isBlank()) {
            httpsession.setAttribute("redirectAfterLogin", redirect);
            log.info("리다이렉트 대상 저장: {}", redirect);
        }

        model.addAttribute("loginForm", new LoginFormDTO());
        return "user/login";
    }

    @GetMapping("/auth/logout")
    public String logoutPage(HttpSession session, HttpServletResponse response) {
        if (session != null) session.invalidate();

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        return "redirect:/auth/login?logout=true"; // 로그인 페이지로 이동
    }


    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password,
                                   HttpSession session) {
        User user = userService.findByUserId(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("로그인 실패");
        }
        session.setAttribute("user", user);
        return ResponseEntity.ok("로그인 성공");
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/"); // 루트 경로에서 제거
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경 필수

        response.addCookie(cookie);

        Map<String, String> result = new HashMap<>();
        result.put("message", "로그아웃 완료");

        return ResponseEntity.ok(result);
    }



    // 회원가입 폼
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
            return "redirect:/auth/login?registerSuccess=true";
        } catch (Exception e) {
            log.error("회원가입 오류: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("registrationSuccess", false);
            redirectAttributes.addFlashAttribute("joinRequestDTO", jdto);
            return "redirect:/register";
        }
    }

    @GetMapping("/api/user/check-id")
    public Map<String, Boolean> checkUserIdDuplicate(@RequestParam String userId) {
        boolean duplicate = userService.isUserIdDuplicate(userId);
        return Map.of("duplicate", duplicate);
    }
}
