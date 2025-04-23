package kroryi.bus2.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/api-key")
@Tag(name = "사용자 API 키")
@Log4j2
@RequiredArgsConstructor
public class UserApiKeyController {

    private final ApiKeyService apiKeyService;
    private final UserService userService;

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
