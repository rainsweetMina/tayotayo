package kroryi.bus2.controller.mypage;

import jakarta.validation.Valid;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.dto.lost.FoundItemListResponseDTO;
import kroryi.bus2.dto.lost.FoundItemResponseDTO;
import kroryi.bus2.dto.lost.LostItemListResponseDTO;
import kroryi.bus2.dto.lost.LostItemRequestDTO;
import kroryi.bus2.dto.mypage.ChangePasswordDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.mypage.FavoriteBusStop;
import kroryi.bus2.entity.mypage.FavoriteRoute;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.apikey.ApiKeyService;
import kroryi.bus2.service.lost.FoundItemServiceImpl;
import kroryi.bus2.service.lost.LostItemService;
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

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserService userService;
    private final ApiKeyService apiKeyService;
    private final LostItemService lostItemService;
    private final FoundItemServiceImpl foundItemServiceImpl;


    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUserId();
        } else if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof OAuth2User oAuth2User) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Object userId = attributes.get("id");
            if (userId != null) {
                return userId.toString();
            }
        }
        return "admin"; // 기본값으로 admin을 리턴
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("사용자가 로그인되지 않았습니다.");
        }
        return userService.findByUserId(principal.getName());
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
        model.addAttribute("user", user);

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

    // GET: API 키 신청 페이지
    @GetMapping("/apikey-request")
    public String showApiKeyRequestForm(Model model) {
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        // 사용자 ID로 API 키를 조회
        Optional<ApiKey> apiKeyOpt = apiKeyService.findLatestByUserId(userId);

        log.info("✅ API 키 조회 결과: {}", apiKeyOpt.isPresent() ? "발급된 API 키 있음" : "발급된 API 키 없음");

        if (apiKeyOpt.isPresent()) {
            model.addAttribute("apiKey", apiKeyOpt.get());
        } else {
            model.addAttribute("apiKey", null);
            model.addAttribute("message", "현재 발급된 API 키가 없습니다. API 키를 신청해 주세요.");
        }

        return "mypage/apikey-request";
    }

    // POST: API 키 신청 처리
    @PostMapping("/apikey-request")
    public String requestApiKey(RedirectAttributes redirectAttributes) {
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            apiKeyService.requestApiKey(userId); // API 키 신청 처리 (reason 파라미터 없이)
            redirectAttributes.addFlashAttribute("message", "API 키 신청이 완료되었습니다. 승인을 기다려주세요.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mypage/apikey-request";
    }

    // ✅ 일반회원 마이페이지: 분실물 목록 및 등록 화면
    @GetMapping("/lost")
    public String userLostItems(Model model) {
        List<LostItemListResponseDTO> lostItems = lostItemService.getAllLostItems();
        model.addAttribute("lostItems", lostItems);
        return "/mypage/mypage-lost"; //
    }
    //분실물 등록 처리
    @PostMapping("/lost")
    public String registerLostItem(LostItemRequestDTO dto) {
        String userId = extractUserId();
        Long memberId = userService.findByUserId(userId).getId();
        dto.setReporterId(memberId);
        lostItemService.saveLostItem(dto);
        return "redirect:/mypage/lost";
    }
    //습득물 목록
    @GetMapping("/found")
    public String foundListForUser(Model model) {
        List<FoundItemResponseDTO> foundItems = foundItemServiceImpl.getVisibleFoundItemsForUser();
        model.addAttribute("foundItems", foundItems);
        return "mypage/mypage-found"; // ✅ Thymeleaf 파일명
    }








}
