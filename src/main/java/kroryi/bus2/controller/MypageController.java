package kroryi.bus2.controller;

import jakarta.validation.Valid;
import kroryi.bus2.dto.mypage.ChangePasswordDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.entity.user.SignupType;
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

import java.security.Principal;

// ... 생략된 import는 그대로 두고 ...

@Log4j2
@Controller
@RequiredArgsConstructor
public class MypageController {

    private final UserService userService;

    // 마이페이지
    @GetMapping("/mypage")
    public String myPage(Model model, Principal principal) {
        // 로그인 여부 확인
        if (principal == null || principal.getName() == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 이동
        }

        String userId = principal.getName();

        try {
            User user = userService.findByUserId(userId);
            if (user == null) {
                return "redirect:/login"; // 사용자가 존재하지 않으면 로그인 페이지로
            }

            model.addAttribute("user", user);
            return "mypage/main"; // 뷰 이름
        } catch (Exception e) {
            // 예외 발생 시 로그인 페이지 또는 에러 페이지로 이동
            return "redirect:/login";
        }
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

        // 👉 여기서 유저 정보 조회
        User user = userService.findByUserId(userId);

        // ✅ 카카오 또는 구글 소셜 로그인 사용자일 경우 비밀번호 변경 막기
        if (user.getSignupType() == SignupType.KAKAO || user.getSignupType() == SignupType.GOOGLE) {
            model.addAttribute("error", "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
            return "mypage/password";
        }

        // 비밀번호 일치 확인
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

            redirectAttributes.addFlashAttribute("success", "회원 정보가 성공적으로 수정되었습니다.");
            return "redirect:/mypage";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "mypage/modify";
        }
    }

    // 즐겨찾기 페이지
    @GetMapping("/favorites")
    public String favorites() {
        return "mypage/favorites";
    }

    // 분실물 신고 페이지
    @GetMapping("/lost-report")
    public String lostReport() {
        return "mypage/lost-report";
    }

    // 질문과 답변 페이지
    @GetMapping("/qna")
    public String qna() {
        return "mypage/qna";
    }

    // 최근 검색 내역 페이지
    @GetMapping("/recent-searches")
    public String recentSearches() {
        return "mypage/recent-searches";
    }
}
