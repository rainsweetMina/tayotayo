package kroryi.bus2.controller;

import kroryi.bus2.dto.user.LoginFormDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class LoginController {

    @GetMapping("/login")
    public String login(String errorCode, String logout, Model model) {
        log.info("로그인 페이지 요청");
        log.info("logout: {}", logout);

        if (logout != null) {
            log.info("로그아웃");
        }

        model.addAttribute("loginForm", new LoginFormDTO()); // loginForm 객체 추가

        return "login";
    }
}
