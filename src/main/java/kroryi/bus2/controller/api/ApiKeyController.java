package kroryi.bus2.controller.api;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.apikey.ApiKeyCallbackUrl;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.apikey.ApiKeyCallbackUrlRepository;
import kroryi.bus2.repository.jpa.apikey.ApiKeyRepository;
import kroryi.bus2.service.apikey.ApiKeyService;
import kroryi.bus2.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ApiKeyController {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyCallbackUrlRepository callbackUrlRepository;
    private final JwtTokenUtil jwtTokenUtil;
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

    // ================================
    // ✅ 관리자 영역 (/admin/apikey)
    // ================================

    @GetMapping("/admin/apikey/dashboard")
    public String dashboard(Model model) {
        List<ApiKey> recent = apiKeyRepository.findAll(Sort.by(Sort.Direction.DESC, "issuedAt"));
        model.addAttribute("recentKeys", recent);
        return "api/apiKeyDashboard";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/apikeys")
    public String getApiKeyList(Model model) {
        List<ApiKey> apiKeyList = apiKeyRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));
        model.addAttribute("apiKeyList", apiKeyList);
        return "admin/apikey-list";
    }


    @GetMapping("/admin/apikey/api")
    @ResponseBody
    public List<ApiKey> getAll() {
        return apiKeyRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));
    }

    @ResponseBody
    @GetMapping("/admin/apikey/{id}")
    public ResponseEntity<ApiKey> getApiKey(@PathVariable Long id) {
        return apiKeyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ResponseBody
    @PostMapping("/admin/apikey")
    public ApiKey createKey(@RequestBody CreateApiKeyRequest request) {
        ApiKey key = ApiKey.builder()
                .name(request.name())
                .active(true)
                .issuedAt(LocalDateTime.now())
                .expiresAt(request.expiresAt())
                .allowedIp(request.allowedIp())
                .build();

        if (request.callbackUrls() != null) {
            for (String url : request.callbackUrls()) {
                ApiKeyCallbackUrl cb = ApiKeyCallbackUrl.builder()
                        .url(url)
                        .apiKey(key)
                        .build();
                key.getCallbackUrls().add(cb);
            }
        }

        ApiKey saved = apiKeyRepository.save(key);
        String jwt = jwtTokenUtil.generateToken(saved);
        saved.setApikey(jwt);
        return apiKeyRepository.save(saved);
    }

    @ResponseBody
    @PutMapping("/admin/apikey/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestParam boolean active) {
        return apiKeyRepository.findById(id)
                .map(key -> {
                    key.setActive(active);
                    apiKeyRepository.save(key);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/apikey/{id}/toggle")
    public String toggleApiKey(@PathVariable Long id) {
        apiKeyService.toggleActive(id);
        return "redirect:/admin/apikey/dashboard";
    }

    @ResponseBody
    @DeleteMapping("/admin/apikey/{id}")
    public ResponseEntity<?> deleteKey(@PathVariable Long id) {
        if (!apiKeyRepository.existsById(id)) return ResponseEntity.notFound().build();
        apiKeyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseBody
    @PostMapping("/admin/apikey/{id}/callback-urls")
    @Transactional
    public ResponseEntity<?> addCallbackUrl(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String url = body.get("url");
        ApiKey key = apiKeyRepository.findById(id).orElse(null);
        if (key == null) return ResponseEntity.notFound().build();

        ApiKeyCallbackUrl cb = new ApiKeyCallbackUrl();
        cb.setUrl(url);
        cb.setApiKey(key);
        callbackUrlRepository.save(cb);
        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @DeleteMapping("/admin/apikey/callback-urls/{callbackId}")
    public ResponseEntity<?> deleteCallbackUrl(@PathVariable Long callbackId) {
        if (!callbackUrlRepository.existsById(callbackId)) return ResponseEntity.notFound().build();
        callbackUrlRepository.deleteById(callbackId);
        return ResponseEntity.noContent().build();
    }

    // ================================
    // ✅ 사용자 영역 (/mypage)
    // ================================

    @GetMapping("/mypage/apikey")
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

    // GET: API 키 신청 페이지
    @GetMapping("/mypage/apikey-request")
    public String showApiKeyRequestForm(Model model, HttpServletRequest request) {
        String userId = extractUserId();
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);
        log.info("✅ /mypage/apikey 요청이 들어왔습니다.");
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

    @PostMapping("/mypage/apikey-request")
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



    @ResponseBody
    @GetMapping("/mypage/apikey/{id}/callback-urls")
    public ResponseEntity<?> getCallbackUrls(@PathVariable Long id,
                                             @RequestHeader("Authorization") String authHeader,
                                             @RequestHeader("callbackUrl") String callbackUrl) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Claims claims = jwtTokenUtil.parseToken(token);
            String keyIdFromToken = claims.getSubject();

            if (!keyIdFromToken.equals(String.valueOf(id))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: ID mismatch");
            }

            List<ApiKeyCallbackUrl> urls = callbackUrlRepository.findByApiKey_Apikey(String.valueOf(id));
            return ResponseEntity.ok(urls);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

    // ================================
    // ✅ 내부 DTO
    // ================================

    public record CreateApiKeyRequest(
            String name,
            String allowedIp,
            LocalDateTime expiresAt,
            List<String> callbackUrls
    ) {}

    public record UpdateApiKeyStatusRequest(
            boolean active
    ) {}

    @ResponseBody
    @GetMapping("/apikey/status")
//    test code
    public ResponseEntity<String> getApiKeyStatus() {
        return ResponseEntity.ok("OK");
    }
}
