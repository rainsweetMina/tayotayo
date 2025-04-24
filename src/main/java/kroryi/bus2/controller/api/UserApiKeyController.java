package kroryi.bus2.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.dto.apiKey.CreateApiKeyRequestDTO;
import kroryi.bus2.dto.apikey.ApiKeyResponseDTO;
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
@RequestMapping("/api/user/api-key")
@Tag(name = "사용자 API 키")
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

        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        return principal.getUserId();
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

    // GET: API 키 신청 폼 페이지
    @Operation(summary = "API 키 신청 페이지", description = "사용자가 API 키를 신청할 수 있는 폼을 보여줍니다.")
    @GetMapping("/apikey-request")
    public String showApiKeyRequestForm(Model model, HttpServletRequest request) {
        String userId = extractUserId();
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);
        log.info("✅ /mypage/apikey 요청이 들어왔습니다.");
        if (userId == null) {
            return "redirect:/login";
        }

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

    // POST: API 키 신청 요청
    @Operation(summary = "API 키 신청 처리", description = "사용자가 API 키를 신청합니다. 신청 후 관리자의 승인을 기다려야 합니다.")
    @PostMapping("/apikey-request")
    public String requestApiKey(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            apiKeyService.requestApiKey(userDetails.getUsername());
            redirectAttributes.addFlashAttribute("message", "API 키 신청이 완료되었습니다. 관리자의 승인을 기다려주세요.");
        } catch (Exception e) {
            log.error("API 키 신청 실패", e);
            redirectAttributes.addFlashAttribute("error", "API 키 신청에 실패했습니다. 다시 시도해주세요.");
        }

        return "redirect:/mypage/apikey-request"; // 리다이렉트 후 메시지 전달
    }

    @Operation(summary = "API 키 발급 요청", description = "사용자가 새로운 API 키 발급을 요청합니다.")
    @PostMapping("/request")
    public ResponseEntity<ApiKeyResponseDTO> requestApiKey(@RequestBody CreateApiKeyRequestDTO request) {
        User user = userService.getUserById(request.getUserId());
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
    @GetMapping("/GetApiKey")
    public ResponseEntity<ApiKeyResponseDTO> getUserApiKey(@RequestParam Long userId) {
        User user = userService.getUserById(userId);
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
    public ResponseEntity<ApiKeyResponseDTO> getUserApiKeyRequest(@RequestParam Long userId) {
        User user = userService.getUserById(userId);
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
