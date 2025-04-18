package kroryi.bus2.controller.api;

import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.service.apikey.ApiKeyService;
import kroryi.bus2.repository.jpa.apikey.ApiKeyRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/api-key")
@Log4j2
public class UserApiKeyController {

    private final ApiKeyService apiKeyService;
    private final ApiKeyRepository apiKeyRepository;

    public UserApiKeyController(ApiKeyService apiKeyService, ApiKeyRepository apiKeyRepository) {
        this.apiKeyService = apiKeyService;
        this.apiKeyRepository = apiKeyRepository;
    }

    /**
     * 사용자 API 키 요청
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestApiKey(@RequestBody CreateApiKeyRequest request) {
        // 사용자 API 키 발급 요청
        ApiKey apiKey = apiKeyService.issueApiKey(request.getName(), request.getAllowedIp());
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKey);
    }

    // 요청용 DTO
    public static class CreateApiKeyRequest {
        private String name;
        private String allowedIp;

        // Getter, Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAllowedIp() {
            return allowedIp;
        }

        public void setAllowedIp(String allowedIp) {
            this.allowedIp = allowedIp;
        }
    }
}
