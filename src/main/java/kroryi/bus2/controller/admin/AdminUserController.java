package kroryi.bus2.controller.admin;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.apiKey.ApiKeyResponseDTO;
import kroryi.bus2.dto.user.UserListResponseDTO;
import kroryi.bus2.dto.user.UserRoleChangeResponseDTO;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.admin.AdminUserService;
import kroryi.bus2.service.apikey.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "사용자-관리", description = "관리자 전용 사용자 관리 API")
@Controller
@RequestMapping
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final ApiKeyService apiKeyService;

    // ✅ 사용자 JSON 목록 (프론트엔드 Vue용)
    @ResponseBody
    @Operation(summary = "Vue용 사용자 목록 API", description = "Vue에서 관리자용 사용자 목록 요청 시 사용")
    @GetMapping("/api/admin/user")
    public List<UserListResponseDTO> getAllUsersForAdmin() {
        return adminUserService.getAllUsers().stream()
                .map(UserListResponseDTO::from)
                .toList();
    }

    // ✅ API 키 JSON 목록 (Vue용)
    @ResponseBody
    @Operation(summary = "Vue용 API 키 목록 API", description = "Vue에서 관리자용 API 키 목록 요청 시 사용")
    @GetMapping("/api/admin/apikey")
    public List<ApiKeyResponseDTO> getAllApiKeysForAdmin() {
        return apiKeyService.getAllApiKeys().stream()
                .map(ApiKeyResponseDTO::fromEntity)
                .toList();
    }

    // ✅ 권한 변경 (Vue용)
    @ResponseBody
    @PostMapping("/api/admin/user/{userId}/role")
    public UserRoleChangeResponseDTO changeUserRoleApi(@PathVariable String userId,
                                                       @RequestParam Role role) {
        adminUserService.changeUserRole(userId, role);
        return new UserRoleChangeResponseDTO(
                userId,
                role,
                userId + "의 권한이 " + role + "(으)로 변경되었습니다."
        );
    }

    // ✅ 기존 Swagger 연동 및 페이지용도 (템플릿 기반)
    @ResponseBody
    @Operation(summary = "사용자 목록 조회", description = "전체 사용자 목록 또는 키워드로 검색된 사용자 목록을 반환합니다.")
    @GetMapping("/api/management/user")
    public List<UserListResponseDTO> userList(@RequestParam(required = false) String keyword) {
        List<User> users = (keyword == null || keyword.isBlank())
                ? adminUserService.getAllUsers()
                : adminUserService.searchUsers(keyword);

        return users.stream()
                .map(UserListResponseDTO::from)
                .toList();
    }

    @PostMapping("/api/management/user/{userId}/role")
    @ResponseBody
    public UserRoleChangeResponseDTO changeUserRole(@PathVariable String userId,
                                                    @RequestParam Role role) {
        adminUserService.changeUserRole(userId, role);
        return new UserRoleChangeResponseDTO(
                userId,
                role,
                userId + "의 권한이 " + role + "(으)로 변경되었습니다."
        );
    }

    @GetMapping("/admin/apikey")
    public String showApiKeyDashboard(Model model) {
        model.addAttribute("recentKeys", apiKeyService.getAllApiKeys());
        return "api/apiKeyDashboard";
    }

    @PostMapping("/admin/apikey/{id}/toggle")
    public String toggleApiKey(@PathVariable Long id) {
        apiKeyService.toggleActiveStatus(id);
        return "redirect:/admin/apikey";
    }

    @Hidden
    @GetMapping("/admin/user/{userId}/signup-date")
    public ResponseEntity<LocalDate> getUserSignupDate(@PathVariable String userId) {
        LocalDate signupDate = adminUserService.getSignupDate(userId);
        return ResponseEntity.ok(signupDate);
    }

    @Hidden
    @GetMapping("/admin/user")
    public String userListPage(@RequestParam(required = false) String keyword, Model model) {
        List<User> users = (keyword == null || keyword.isBlank())
                ? adminUserService.getAllUsers()
                : adminUserService.searchUsers(keyword);

        model.addAttribute("users", users);
        return "admin/user-list";
    }

    @Hidden
    @PostMapping("/admin/users/{userId}/temp-password")
    public String generateTempPassword(@PathVariable String userId, RedirectAttributes redirectAttributes) {
        String tempPassword = adminUserService.generateTemporaryPassword(userId);
        redirectAttributes.addFlashAttribute("message", userId + "의 임시 비밀번호: " + tempPassword);
        return "redirect:/admin/user";
    }

    @Hidden
    @PostMapping("/admin/users/{userId}/withdraw")
    public String withdrawUser(@PathVariable String userId, RedirectAttributes redirectAttributes) {
        adminUserService.withdrawUser(userId);
        redirectAttributes.addFlashAttribute("message", userId + "님이 탈퇴 처리되었습니다.");
        return "redirect:/admin/user";
    }

    @Hidden
    @PostMapping("/api/admin/user/{userId}/temp-password")
    @ResponseBody
    public Map<String, String> generateTempPasswordForVue(@PathVariable String userId) {
        String tempPassword = adminUserService.generateTemporaryPassword(userId);
        return Map.of(
                "userId", userId,
                "tempPassword", tempPassword
        );
    }

}
