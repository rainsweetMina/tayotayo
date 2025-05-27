package kroryi.bus2.controller.mypage;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.dto.lost.*;
import kroryi.bus2.dto.mypage.ChangePasswordDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.apikey.ApiKeyService;
import kroryi.bus2.service.lost.LostItemService;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Hidden
@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserService userService;
    private final LostItemService lostItemService;
    private final ApiKeyService apiKeyService;

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

    // 마이페이지 메인
    @Operation(summary = "마이페이지 메인", description = "현재 로그인된 사용자의 마이페이지 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 마이페이지 정보를 조회했습니다."),
            @ApiResponse(responseCode = "401", description = "로그인되지 않은 사용자입니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
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

    // GET: 발급된 API 키 확인 페이지
    @Operation(summary = "API 키 조회 페이지", description = "로그인한 사용자의 API 키를 확인할 수 있는 페이지입니다.")
    @GetMapping("/apikey")
    public String showApiKey(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.warn("🛑 사용자 정보가 없습니다.");
            return "redirect:/login";
        }

        User user = userDetails.getUser();
        ApiKey apiKey = apiKeyService.getApiKeyForUser(user);

        model.addAttribute("apiKey", apiKey);
        model.addAttribute("parameterName", "Your Parameter Value"); // 원하는 값 넣기
        return "mypage/apikey-request";
    }

    // API 키 신청 화면 보여주기
    @GetMapping("/apikey-request")
    public String showApiKeyRequestForm(Model model) {
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findByUserId(userId);
        if (user == null) {
            return "redirect:/login";
        }

        // APPROVED, PENDING 구분 없이 최근 키 1개 조회
        ApiKey apiKey = apiKeyService.getApiKeyRequestForUser(user);
        log.info("✅ API 키 조회 결과: {}", (apiKey != null ? "발급된 API 키 있음" : "발급된 API 키 없음"));

        model.addAttribute("apiKey", apiKey);

        if (apiKey == null) {
            model.addAttribute("message", "현재 발급된 API 키가 없습니다. API 키를 신청해 주세요.");
        }

        return "mypage/apikey-request";
    }


    // API 키 발급 요청 페이지
    @PostMapping("/apikey-request")
    public String handleApiKeyRequest(@RequestParam String type, RedirectAttributes redirectAttributes) {
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            if ("request".equals(type)) {
                apiKeyService.requestApiKey(userId);
                redirectAttributes.addFlashAttribute("message", "API 키 신청이 완료되었습니다.");
            } else if ("renew".equals(type)) {
                apiKeyService.reissueApiKey(userId);
                redirectAttributes.addFlashAttribute("message", "API 키가 재발급되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "잘못된 요청입니다.");
            }
        } catch (Exception e) {
            log.error("API 키 신청 실패", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mypage/apikey-request";
    }


    // 비밀번호 변경 폼
    @Operation(summary = "비밀번호 변경 폼", description = "비밀번호 변경 폼을 표시합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 폼을 성공적으로 표시했습니다."),
            @ApiResponse(responseCode = "401", description = "로그인되지 않은 사용자입니다.")
    })
    @GetMapping("/password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        return "mypage/password";
    }

    // 비밀번호 변경 처리
    @Operation(summary = "비밀번호 변경 처리", description = "사용자의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호가 성공적으로 변경되었습니다."),
            @ApiResponse(responseCode = "400", description = "비밀번호 변경 오류")
    })
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
    @Operation(summary = "회원 정보 수정 폼", description = "사용자의 정보를 수정할 수 있는 폼을 표시합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 폼을 성공적으로 표시했습니다."),
            @ApiResponse(responseCode = "401", description = "로그인되지 않은 사용자입니다.")
    })
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
    @Operation(summary = "회원 정보 수정 처리", description = "사용자의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "회원 정보 수정 오류")
    })
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

    // ✅ 일반회원 마이페이지: 분실물 목록 및 등록 화면
    @Operation(summary = "분실물 목록 조회", description = "사용자의 분실물 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "분실물 목록 조회 성공")
    })
    @GetMapping("/lost")
    public String userLostItems(Model model) {
        // ✅ 1. 로그인 유저 ID 추출
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        // ✅ 2. 유저 객체 가져오기
        User user = userService.findByUserId(userId);
        if (user == null) {
            return "redirect:/login";
        }

        // ✅ 3. 본인 분실물만 조회
        List<LostItemResponseDTO> lostItems = lostItemService.getMyLostItems(user.getId());

        // ✅ 4. 모델에 담기
        model.addAttribute("lostItems", lostItems);

        return "mypage/mypage-lost"; // ✅ 기존 뷰 그대로 사용
    }


    // 🔄 상세보기 요청 시 숨김/삭제된 글이면 에러 메시지와 함께 목록으로 redirect
    @GetMapping("/lost/view/{id}")
    public String viewLostItem(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            LostItemResponseDTO dto = lostItemService.getLostItemById(id);
            model.addAttribute("lostItem", dto);
            return "mypage/mypageLostdetail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "이 게시물은 숨김 또는 삭제 처리되어 확인할 수 없습니다.");
            return "redirect:/mypage/lost";
        }
    }

    @GetMapping("/lost/edit/{id}")
    public String editLostItemForm(@PathVariable Long id, Model model) {
        LostItemResponseDTO dto = lostItemService.getLostItemById(id);
        LostItemEditDTO editDTO = LostItemEditDTO.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .busNumber(dto.getBusNumber())
                .busCompany(dto.getBusCompany())
                .lostTime(dto.getLostTime())
                .build();

        model.addAttribute("lostItem", editDTO);
        return "mypage/mypageLostEdit";
    }

    @PostMapping("/lost/edit/{id}")
    public String updateLostItem(@PathVariable Long id,
                                 @ModelAttribute LostItemEditDTO dto) {
        LostItemRequestDTO requestDTO = LostItemRequestDTO.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .busNumber(dto.getBusNumber())
                .busCompany(dto.getBusCompany())
                .lostTime(dto.getLostTime())
                .build();

        lostItemService.updateLostItem(id, requestDTO);
        return "redirect:/mypage/lost/view/" + id;
    }
    @PostMapping("/lost/delete/{id}")
    public String deleteLostItem(@PathVariable Long id) {
        lostItemService.deleteLostItem(id); // 이 메서드는 soft delete 혹은 삭제 처리
        return "redirect:/mypage/lost";
    }

    @GetMapping("/withdraw")
    public String showWithdrawPage() {
        return "mypage/withdraw";
    }

    @Operation(summary = "회원 탈퇴 처리", description = "현재 로그인된 사용자를 탈퇴 처리합니다.")
    @PostMapping("/withdraw")
    public String handleWithdraw(RedirectAttributes redirectAttributes, Model model) {
        String userId = extractUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        userService.withdrawUser(userId);
        SecurityContextHolder.clearContext(); // 로그아웃
        model.addAttribute("message", "회원 탈퇴가 완료되었습니다.");
        return "mypage/withdraw"; // ✅ HTML 직접 렌더링
    }


}
