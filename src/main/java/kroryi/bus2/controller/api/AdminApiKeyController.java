package kroryi.bus2.controller.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.apiKey.CreateApiKeyRequestDTO;
import kroryi.bus2.dto.apiKey.ApiKeyResponseDTO;
import kroryi.bus2.dto.apiKey.UpdateApiKeyStatusRequestDTO;
import kroryi.bus2.entity.apikey.ApiKey;
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

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/admin/apikey")
@Tag(name = "관리자-API키-관리", description = "관리자용 API 키 발급 및 관리 기능 제공")
public class AdminApiKeyController {

    private final ApiKeyRepository apiKeyRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final ApiKeyService apiKeyService;

    @Hidden
    @Operation(summary = "API 키 대시보드 (뷰)", description = "최근 발급된 API 키 목록을 대시보드 뷰로 반환합니다.")
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<ApiKey> recent = apiKeyRepository.findAll(Sort.by(Sort.Direction.DESC, "issuedAt"));
        model.addAttribute("recentKeys", recent);
        return "api/apiKeyDashboard";
    }

    @Operation(summary = "API 키 전체 목록 조회", description = "모든 API 키를 조회하는 관리자용 뷰를 반환합니다.")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
                    dto.setCreatedAt(apiKey.getCreatedAt());
                    dto.setExpiresAt(apiKey.getExpiresAt());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }


    @Operation(summary = "단일 API 키 조회", description = "지정한 ID에 해당하는 API 키 정보를 반환합니다.")
    @GetMapping("/{id}")
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
    @PostMapping("/create")
    public ApiKeyResponseDTO createKey(@RequestBody CreateApiKeyRequestDTO request) {
        ApiKey key = ApiKey.builder()
                .user_name(request.getUser_name())
                .active(true)
                .issuedAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .allowedIp(request.getAllowedIp())
                .build();

        if (request.getCallbackUrls() != null) {
            for (String url : request.getCallbackUrls()) {
                key.addCallbackUrl(url);
            }
        }

        ApiKey saved = apiKeyRepository.save(key);
        String jwt = jwtTokenUtil.generateToken(saved);
        saved.setApikey(jwt);

        ApiKeyResponseDTO response = new ApiKeyResponseDTO();
        response.setId(saved.getId());
        response.setUsername(saved.getUser_name());
        response.setActive(saved.isActive());
        response.setApiKey(saved.getApiKey());
        return response;
    }

    @Operation(summary = "API 키 상태 변경", description = "지정한 API 키의 활성화 상태를 변경합니다.")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestBody UpdateApiKeyStatusRequestDTO request) {
        return apiKeyRepository.findById(id)
                .map(key -> {
                    key.setActive(request.isActive());
                    apiKeyRepository.save(key);
                    return ResponseEntity.ok().body("API 키 상태가 변경되었습니다.");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("API 키를 찾을 수 없습니다."));
    }



    @Operation(summary = "API 키 삭제", description = "지정한 ID의 API 키를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteKey(@PathVariable Long id) {
        if (!apiKeyRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 ID의 API 키를 찾을 수 없습니다.");
        }

        apiKeyRepository.deleteById(id);
        return ResponseEntity.ok("API 키가 성공적으로 삭제되었습니다.");
    }
}
