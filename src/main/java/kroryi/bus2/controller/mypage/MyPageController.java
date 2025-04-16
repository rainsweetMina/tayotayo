package kroryi.bus2.controller.mypage;

import jakarta.validation.Valid;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.dto.mypage.ChangePasswordDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserService userService;

    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUserId();
        } else if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof OAuth2User oAuth2User) {
            // 혹시 다른 OAuth2User 타입으로 들어왔을 경우 보완
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Object userId = attributes.get("id"); // 또는 CustomOAuth2User에서 넣어준 키
            if (userId != null) {
                return userId.toString(); // fallback
            }
        }

//        return null;
        // ✅ 개발 중: 로그인 안 해도 테스트 가능하게 null 대신 기본값 리턴
        return "admin";
    }


    // 마이페이지 메인
    @GetMapping("")
    public String myPage(Model model) {
        String userId = extractUserId();

        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findByUserId(userId);
        if (user == null) {
            return "redirect:/login";
        }

        log.info("✅ 현재 로그인된 사용자 ID: {}", userId);
        model.addAttribute("user", user); // ✅ 사용자 정보 추가

        return "mypage/index";
    }


    // 비밀번호 변경 폼
    @GetMapping("/password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        return "mypage/password";
    }

    // 비밀번호 변경 처리
    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordDTO dto, Model model) {
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findByUserId(userId);
        if (user == null) {
            return "redirect:/login";
        }

        if (user.getSignupType() == SignupType.KAKAO || user.getSignupType() == SignupType.GOOGLE) {
            model.addAttribute("error", "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
            return "mypage/password";
        }

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

    // 회원 정보 수정 폼
    @GetMapping("/modify")
    public String showModifyForm(Model model) {
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findByUserId(userId);
        if (user == null) {
            return "redirect:/login";
        }

        ModifyUserDTO dto = new ModifyUserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setSignupType(user.getSignupType());
        dto.setSignupDate(user.getSignupDate());
        dto.setRole(user.getRole());

        model.addAttribute("modifyUserDTO", dto);
        model.addAttribute("socialUser", user.getSignupType() == SignupType.KAKAO || user.getSignupType() == SignupType.GOOGLE);
        return "mypage/modify";
    }

    // 회원 정보 수정 처리
    @PostMapping("/modify")
    public String modifyUser(@Valid @ModelAttribute ModifyUserDTO dto,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

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

    // 즐겨찾기
    @GetMapping("/favorites")
    public String favorites() {
        return "mypage/favorites";
    }

    // 분실물 신고
    @GetMapping("/lost-report")
    public String lostReport() {
        return "mypage/lost-report";
    }

    // Q&A
    @GetMapping("/qna")
    public String qna() {
        return "mypage/qna";
    }

    // 최근 검색 내역
    @GetMapping("/recent-searches")
    public String recentSearches() {
        return "mypage/recent-searches";
    }

    // 회원 탈퇴
    @PostMapping("/withdraw")
    public String withdraw(RedirectAttributes redirectAttributes) {
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            userService.deleteByUserId(userId); // 탈퇴 처리
            SecurityContextHolder.clearContext();
            redirectAttributes.addFlashAttribute("success", "회원 탈퇴가 완료되었습니다.");
            return "redirect:/login?withdraw";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "회원 탈퇴 중 오류가 발생했습니다.");
            return "redirect:/mypage";
        }
    }
}
