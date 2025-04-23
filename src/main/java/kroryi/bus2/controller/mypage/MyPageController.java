package kroryi.bus2.controller.mypage;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import kroryi.bus2.dto.lost.*;
import kroryi.bus2.dto.mypage.ChangePasswordDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
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

@Log4j2
@Controller
@Hidden
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserService userService;
    private final LostItemService lostItemService;
    private final FoundItemServiceImpl foundItemServiceImpl;

    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
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
        List<LostItemListResponseDTO> lostItems = lostItemService.getAllLostItems();
        model.addAttribute("lostItems", lostItems);
        return "/mypage/mypage-lost";
    }

    // 분실물 등록 처리
    @Operation(summary = "분실물 등록", description = "새로운 분실물을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "분실물이 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "분실물 등록 오류")
    })
    @PostMapping("/lost")
    public String registerLostItem(LostItemRequestDTO dto) {
        String userId = extractUserId();
        Long memberId = userService.findByUserId(userId).getId();
        dto.setReporterId(memberId);
        lostItemService.saveLostItem(dto);
        return "redirect:/mypage/lost";
    }

    // 습득물 목록
    @Operation(summary = "습득물 목록 조회", description = "사용자가 조회할 수 있는 습득물 목록을 표시합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "습득물 목록 조회 성공")
    })
    @GetMapping("/found")
    public String foundListForUser(Model model) {
        List<FoundItemResponseDTO> foundItems = foundItemServiceImpl.getVisibleFoundItemsForUser();
        model.addAttribute("foundItems", foundItems);
        return "mypage/mypage-found"; // ✅ Thymeleaf 파일명
    }
    @GetMapping("/lost/view/{id}")
    public String viewLostItem(@PathVariable Long id, Model model) {
        LostItemResponseDTO dto = lostItemService.getLostItemById(id);
        model.addAttribute("lostItem", dto);
        return "mypage/mypageLostdetail";
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

}

