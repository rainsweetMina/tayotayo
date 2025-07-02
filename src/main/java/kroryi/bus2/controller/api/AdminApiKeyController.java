package kroryi.bus2.controller.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.aop.AdminTracked;
import kroryi.bus2.dto.apiKey.CreateApiKeyRequestDTO;
import kroryi.bus2.dto.apiKey.ApiKeyResponseDTO;
import kroryi.bus2.dto.apiKey.UpdateApiKeyStatusRequestDTO;
import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.apikey.ApiKeyStatus;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.apikey.ApiKeyService;
import kroryi.bus2.repository.jpa.apikey.ApiKeyRepository;
import kroryi.bus2.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/admin")
@Tag(name = "관리자-API키-관리", description = "관리자용 API 키 발급 및 관리 기능 제공")
public class AdminApiKeyController {

    private final ApiKeyRepository apiKeyRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final ApiKeyService apiKeyService;

    @Hidden
    @Operation(summary = "API 키 대시보드 (뷰)", description = "최근 발급된 API 키 목록을 대시보드 뷰로 반환합니다.")
    @GetMapping("/apikey/dashboard")
    @AdminAudit(action = "API 키 대시보드 조회", target = "ApiKey")
    public String dashboard(Model model) {
        List<ApiKey> recent = apiKeyRepository.findAll(Sort.by(Sort.Direction.DESC, "issuedAt"));
        model.addAttribute("recentKeys", recent);
        return "api/apiKeyDashboard";
    }

    @Operation(summary = "API 키 전체 목록 조회", description = "모든 API 키를 조회하는 관리자용 뷰를 반환합니다.")
    @GetMapping("/apikey/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @AdminAudit(action = "API 키 목록 조회", target = "ApiKey")
    public ResponseEntity<List<ApiKeyResponseDTO>> getApiKeyList() {
        List<ApiKey> apiKeyList = apiKeyRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));

        // ApiKey 객체를 ApiKeyResponseDTO로 변환
        List<ApiKeyResponseDTO> responseList = apiKeyList.stream()
                .map(apiKey -> {
                    ApiKeyResponseDTO dto = new ApiKeyResponseDTO();
                    dto.setId(apiKey.getId());
                    dto.setUsername(apiKey.getUser().getUsername());
                    dto.setApiKey(apiKey.getApiKey());
                    dto.setActive(apiKey.isActive());
                    dto.setStatus(apiKey.getStatus());
                    dto.setCreatedAt(apiKey.getCreatedAt());
                    dto.setExpiresAt(apiKey.getExpiresAt());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "단일 API 키 조회", description = "지정한 ID에 해당하는 API 키 정보를 반환합니다.")
    @GetMapping("/apikey/{id}")
    @AdminAudit(action = "API 키 단일 조회", target = "ApiKey")
    public ResponseEntity<ApiKeyResponseDTO> getApiKey(@PathVariable Long id) {
        return apiKeyRepository.findById(id)
                .map(apiKey -> {
                    ApiKeyResponseDTO response = new ApiKeyResponseDTO();
                    response.setId(apiKey.getId());
                    response.setUsername(apiKey.getUser().getUsername());
                    response.setActive(apiKey.isActive());
                    response.setApiKey(apiKey.getApiKey());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "API 키 생성", description = "새로운 API 키를 생성합니다.")
    @PostMapping("/apikey/create")
    public ApiKeyResponseDTO createKey(@RequestBody CreateApiKeyRequestDTO request) {
        ApiKey.ApiKeyBuilder builder = ApiKey.builder()
                .user_name(request.getUser_name())
                .active(true)
                .issuedAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .allowedIp(request.getAllowedIp())
                .status(ApiKeyStatus.PENDING)
                .createdAt(LocalDateTime.now());

        // ✅ userId가 넘어온 경우 User 엔티티 연결
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            User user = apiKeyService.getUserByUserId(request.getUserId());
            builder.user(user);
            builder.userIdString(user.getUserId());
        }

        ApiKey key = builder.build();

        // ✅ 콜백 URL 설정
        if (request.getCallbackUrls() != null) {
            for (String url : request.getCallbackUrls()) {
                key.addCallbackUrl(url);
            }
        }

        ApiKey saved = apiKeyRepository.save(key);

        // ✅ JWT 토큰 생성 및 설정
        String jwt = jwtTokenUtil.generateToken(saved);
        saved.setApikey(jwt);
        apiKeyRepository.save(saved);  // jwt 반영된 상태로 다시 저장

        // ✅ 응답 DTO 생성
        ApiKeyResponseDTO response = new ApiKeyResponseDTO();
        response.setId(saved.getId());
        response.setUsername(saved.getUser_name());
        response.setUserId(saved.getUserIdString());
        response.setActive(saved.isActive());
        response.setApiKey(saved.getApiKey());
        response.setCreatedAt(saved.getCreatedAt());
        response.setExpiresAt(saved.getExpiresAt());

        return response;
    }


    @Operation(summary = "API 키 상태 변경", description = "지정한 API 키의 활성화 상태를 변경합니다.")
    @PutMapping("/apikey/{id}/active")
    @AdminAudit(action = "API 키 활성 상태 변경", target = "ApiKey")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestBody UpdateApiKeyStatusRequestDTO request) {
        return apiKeyRepository.findById(id)
                .map(key -> {
                    key.setActive(request.isActive());
                    apiKeyRepository.save(key);
                    return ResponseEntity.ok().body("API 키 상태가 변경되었습니다.");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("API 키를 찾을 수 없습니다."));
    }

    @Operation(summary = "API 키 승인 상태 토글", description = "API 키 상태를 승인/대기로 토글합니다.")
    @PutMapping("/apikey/{id}/toggle-approval")
    public ResponseEntity<String> toggleApprovalStatus(@PathVariable Long id) {
        log.info("🔁 API 키 승인 상태 변경 요청: {}", id); // ← 로그 추가하면 호출 여부 추적 가능
        boolean approved = apiKeyService.toggleActive(id); // 또는 toggleActiveStatus(id)

        return ResponseEntity.ok(
                approved ? "✅ API 키가 승인되었습니다." : "⏳ API 키가 대기 상태로 변경되었습니다."
        );
    }


    @Operation(summary = "API 키 삭제", description = "지정한 ID의 API 키를 삭제합니다.")
    @DeleteMapping("/apikey/{id}")
    @AdminAudit(action = "API 키 삭제", target = "ApiKey")
    public ResponseEntity<String> deleteKey(@PathVariable Long id) {
        if (!apiKeyRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 ID의 API 키를 찾을 수 없습니다.");
        }

        apiKeyRepository.deleteById(id);
        return ResponseEntity.ok("API 키가 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "API 키 상태 변경", description = "관리자가 API 키의 상태(PENDING, APPROVED, EXPIRED)를 변경합니다.")
    @PostMapping("/apikey/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @AdminAudit(action = "API 키 상태 변경", target = "ApiKey")
    public ResponseEntity<String> updateApiKeyStatus(@PathVariable Long id,
                                                     @RequestParam ApiKeyStatus status) {
        return apiKeyRepository.findById(id)
                .map(apiKey -> {
                    apiKey.setStatus(status);  // 상태만 변경
                    apiKeyRepository.save(apiKey);
                    return ResponseEntity.ok("API 키 상태가 " + status + "로 변경되었습니다.");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("해당 ID의 API 키를 찾을 수 없습니다."));
    }
}
