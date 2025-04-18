package kroryi.bus2.controller.api;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin/apikey")  // 경로 변경
@RequiredArgsConstructor
@Log4j2
public class ApiKeyController {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyCallbackUrlRepository callbackUrlRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final ApiKeyService apiKeyService;

    // ✅ 관리자 대시보드 페이지 (최근 5개의 API 키만 표시)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<ApiKey> recent = apiKeyRepository.findAll(Sort.by(Sort.Direction.DESC, "issuedAt"));
        model.addAttribute("recentKeys", recent);
        return "api/apiKeyDashboard";
    }

    @GetMapping("/admin/apikey")
    public String showApiKeyDashboard(Model model) {
        List<ApiKey> keys = apiKeyService.getAllApiKeys();
        model.addAttribute("apiKeys", keys);
        return "admin/apikey";
    }

    // REST API 목록 조회
    @GetMapping("/admin/apikey/api")
    @ResponseBody
    public List<ApiKey> getAll() {
        return apiKeyRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));
    }

    // ✅ 특정 API 키 조회
    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<ApiKey> getApiKey(@PathVariable Long id) {
        Optional<ApiKey> apiKey = apiKeyRepository.findById(id);
        return apiKey.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ API 키 생성
    @ResponseBody
    @PostMapping
    public ApiKey createKey(@RequestBody CreateApiKeyRequest request) {
        ApiKey key = ApiKey.builder()
                .name(request.name)
                .active(true)
                .issuedAt(LocalDateTime.now())
                .expiresAt(request.expiresAt())
                .allowedIp(request.allowedIp())
                .build();

        if (request.callbackUrls != null) {
            for (String url : request.callbackUrls) {
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

    // ✅ API 키 상태 변경
    @ResponseBody
    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestParam boolean active) {
        Optional<ApiKey> opt = apiKeyRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        ApiKey key = opt.get();
        key.setActive(active);
        apiKeyRepository.save(key);
        return ResponseEntity.ok().build();
    }

    // API 키 활성화/비활성화 상태 변경
    @PostMapping("/{id}/toggle")
    public String toggleApiKey(@PathVariable Long id) {
        apiKeyService.toggleActive(id);  // 상태 변경
        return "redirect:/admin/apikey/dashboard";  // 대시보드로 리다이렉트
    }

    // ✅ API 키 삭제
    @ResponseBody
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKey(@PathVariable Long id) {
        if (!apiKeyRepository.existsById(id)) return ResponseEntity.notFound().build();
        apiKeyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 콜백 URL 추가
    @ResponseBody
    @PostMapping("/{id}/callback-urls")
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

    // ✅ 콜백 URL 삭제
    @ResponseBody
    @DeleteMapping("/callback-urls/{callbackId}")
    public ResponseEntity<?> deleteCallbackUrl(@PathVariable Long callbackId) {
        if (!callbackUrlRepository.existsById(callbackId)) return ResponseEntity.notFound().build();
        callbackUrlRepository.deleteById(callbackId);
        return ResponseEntity.noContent().build();
    }

    // ✅ 특정 API 키의 콜백 URL 목록 조회 (토큰 검증 포함)
    @ResponseBody
    @GetMapping("/{id}/callback-urls")
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

    @GetMapping("/mypage/apikey")
    public String showApiKey(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.warn("🛑 사용자 정보가 없습니다. (userDetails is null)");
            return "redirect:/login";
        }
        User user = userDetails.getUser();  // User 엔티티를 가져옴
        ApiKey apiKey = apiKeyService.getApiKeyForUser(user);

        // 템플릿에서 parameterName을 사용하고자 한다면, 이를 모델에 추가합니다.
        model.addAttribute("apiKey", apiKey);
        model.addAttribute("parameterName", "Your Parameter Value");  // 템플릿에 전달할 변수

        return "mypage/apikey-request";
    }


    @PostMapping("/mypage/apikey-request")
    public String requestApiKey(Model model) {
        try {
            // API 키 신청 로직
            model.addAttribute("message", "API 키 신청이 완료되었습니다. 관리자의 승인을 기다려주세요.");
        } catch (Exception e) {
            model.addAttribute("error", "API 키 신청에 실패했습니다. 다시 시도해주세요.");
        }
        return "mypage/apikey-request";
    }

    // ✅ 요청용 DTO
    public record CreateApiKeyRequest(
            String name,
            String allowedIp,
            LocalDateTime expiresAt,
            List<String> callbackUrls
    ) {}

}
