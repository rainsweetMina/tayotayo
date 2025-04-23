package kroryi.bus2.controller.api;

import kroryi.bus2.dto.apiKey.CreateApiKeyRequestDTO;
import kroryi.bus2.dto.apikey.ApiKeyResponseDTO;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class AdminApiKeyController {

    private final ApiKeyRepository apiKeyRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final ApiKeyService apiKeyService;

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

    @ResponseBody
    @GetMapping("/admin/apikey/{id}")
    public ResponseEntity<ApiKeyResponseDTO> getApiKey(@PathVariable Long id) {
        return apiKeyRepository.findById(id)
                .map(apiKey -> {
                    ApiKeyResponseDTO response = new ApiKeyResponseDTO();
                    response.setId(apiKey.getId());
                    response.setName(apiKey.getName());
                    response.setActive(apiKey.isActive());
                    response.setApiKey(apiKey.getApiKey());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ResponseBody
    @PostMapping("/admin/apikey")
    public ApiKeyResponseDTO createKey(@RequestBody CreateApiKeyRequestDTO request) {
        ApiKey key = ApiKey.builder()
                .name(request.getName())
                .active(true)
                .issuedAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .allowedIp(request.getAllowedIp())
                .build();

        if (request.getCallbackUrls() != null) {
            for (String url : request.getCallbackUrls()) {
                key.addCallbackUrl(url);  // Add callback URLs if provided
            }
        }

        ApiKey saved = apiKeyRepository.save(key);
        String jwt = jwtTokenUtil.generateToken(saved);
        saved.setApikey(jwt);

        ApiKeyResponseDTO response = new ApiKeyResponseDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setActive(saved.isActive());
        response.setApiKey(saved.getApiKey());
        return response;
    }

    @ResponseBody
    @PutMapping("/admin/apikey/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestBody UpdateApiKeyStatusRequestDTO request) {
        return apiKeyRepository.findById(id)
                .map(key -> {
                    key.setActive(request.isActive());
                    apiKeyRepository.save(key);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ResponseBody
    @DeleteMapping("/admin/apikey/{id}")
    public ResponseEntity<?> deleteKey(@PathVariable Long id) {
        if (!apiKeyRepository.existsById(id)) return ResponseEntity.notFound().build();
        apiKeyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
