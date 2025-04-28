package kroryi.bus2.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.dto.apiKey.CreateApiKeyRequestDTO;
import kroryi.bus2.dto.apiKey.ApiKeyResponseDTO;
import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.apikey.ApiKeyService;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@RestController
@RequestMapping("/api/user/apikey")
@Tag(name = "사용자-API키")
@Log4j2
@RequiredArgsConstructor
public class UserApiKeyController {

    private final ApiKeyService apiKeyService;
    private final UserService userService;

    private String extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUserId();
        } else if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        } else {
            throw new IllegalStateException("알 수 없는 사용자 유형입니다.");
        }
    }

    @Operation(summary = "API 키 발급 요청", description = "사용자가 새로운 API 키 발급을 요청합니다.")
    @PostMapping("/request")
    public ResponseEntity<ApiKeyResponseDTO> requestApiKey(@RequestBody CreateApiKeyRequestDTO request) {
        User user = userService.getUserByUserId(request.getUserId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ApiKey apiKey = apiKeyService.issueApiKey(request.getName(), request.getAllowedIp(), user);
        ApiKeyResponseDTO response = new ApiKeyResponseDTO();
        response.setId(apiKey.getId());
        response.setName(apiKey.getName());
        response.setActive(apiKey.isActive());
        response.setApiKey(apiKey.getApiKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "발급된 API 키 조회", description = "사용자의 API 키를 조회합니다.")
    @GetMapping("/getApiKey")
    public ResponseEntity<ApiKeyResponseDTO> getUserApiKey(@RequestParam String userId) {
        User user = userService.getUserByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ApiKey apiKey = apiKeyService.getApiKeyForUser(user);
        if (apiKey == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ApiKeyResponseDTO response = new ApiKeyResponseDTO();
        response.setId(apiKey.getId());
        response.setName(apiKey.getName());
        response.setActive(apiKey.isActive());
        response.setApiKey(apiKey.getApiKey());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "API 키 발급 요청 기록 조회", description = "사용자가 이전에 요청한 API 키 발급 기록을 조회합니다.")
    @GetMapping("/getApiKeyRequest")
    public ResponseEntity<ApiKeyResponseDTO> getUserApiKeyRequest(@RequestParam String userId) {
        User user = userService.getUserByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ApiKey apiKey = apiKeyService.getApiKeyRequestForUser(user);
        if (apiKey == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ApiKeyResponseDTO response = new ApiKeyResponseDTO();
        response.setId(apiKey.getId());
        response.setName(apiKey.getName());
        response.setActive(apiKey.isActive());
        response.setApiKey(apiKey.getApiKey());
        return ResponseEntity.ok(response);
    }
}
