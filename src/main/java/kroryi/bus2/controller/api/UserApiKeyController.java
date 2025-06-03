package kroryi.bus2.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/apikey")
@Tag(name = "사용자-API키")
@Log4j2
@RequiredArgsConstructor
public class UserApiKeyController {

    private final ApiKeyService apiKeyService;
    private final UserService userService;

    // 인증 사용자 ID 추출
    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("인증되지 않은 사용자입니다.");
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomOAuth2User user) return user.getUserId();
        if (principal instanceof CustomUserDetails user) return user.getUserId();
        throw new IllegalStateException("알 수 없는 사용자 유형입니다.");
    }

    // 공통 DTO 변환 메서드
    private ApiKeyResponseDTO toDto(ApiKey apiKey) {
        ApiKeyResponseDTO dto = new ApiKeyResponseDTO();
        dto.setId(apiKey.getId());
        dto.setUsername(apiKey.getUser() != null ? apiKey.getUser().getUsername() : null);
        dto.setUserId(apiKey.getUserIdString());
        dto.setActive(apiKey.isActive());
        dto.setApiKey(apiKey.getApiKey());
        dto.setCreatedAt(apiKey.getCreatedAt());
        dto.setExpiresAt(apiKey.getExpiresAt());
        return dto;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiKeyResponseDTO> requestApiKey(@RequestBody CreateApiKeyRequestDTO request) {
        log.info("🔥 [requestApiKey] 컨트롤러 진입 - userId: {}", request.getUserId());

        User user = userService.getUserByUserId(request.getUserId());
        if (user == null) {
            log.warn("❌ 사용자 없음 - userId: {}", request.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ApiKey apiKey = apiKeyService.issueApiKey(request.getUser_name(), request.getAllowedIp(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(apiKey));
    }

    @Operation(summary = "발급된 API 키 조회", description = "사용자의 API 키를 조회합니다.")
    @GetMapping("/getApiKey")
    public ResponseEntity<ApiKeyResponseDTO> getUserApiKey(@RequestParam String userId) {
        User user = userService.getUserByUserId(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        ApiKey apiKey = apiKeyService.getApiKeyForUser(user);
        if (apiKey == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        return ResponseEntity.ok(toDto(apiKey));
    }

    @Operation(summary = "API 키 재발급 요청", description = "기존 키가 없을 때 새 API 키를 재발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<?> reissueApiKey(@RequestParam String userId) {
        log.info("🔁 [reissueApiKey] API 키 재발급 요청 - userId: {}", userId);

        try {
            ApiKey apiKey = apiKeyService.reissueApiKey(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(apiKey));
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("❌ 재발급 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "API 키 발급 요청 기록 조회", description = "사용자가 이전에 요청한 API 키 발급 기록을 조회합니다.")
    @GetMapping("/getApiKeyRequest")
    public ResponseEntity<ApiKeyResponseDTO> getUserApiKeyRequest(@RequestParam String userId) {
        User user = userService.getUserByUserId(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        ApiKey apiKey = apiKeyService.getApiKeyRequestForUser(user);
        if (apiKey == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        return ResponseEntity.ok(toDto(apiKey));
    }
}
