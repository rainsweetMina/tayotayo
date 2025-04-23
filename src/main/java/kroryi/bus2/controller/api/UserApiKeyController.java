package kroryi.bus2.controller.api;

import kroryi.bus2.dto.apiKey.CreateApiKeyRequestDTO;
import kroryi.bus2.dto.apikey.ApiKeyResponseDTO;
import kroryi.bus2.service.apikey.ApiKeyService;
import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api-key")
@Log4j2
@RequiredArgsConstructor
public class UserApiKeyController {

    private final ApiKeyService apiKeyService;
    private final UserService userService;  // UserService 주입

    @PostMapping("/request")
    public ResponseEntity<ApiKeyResponseDTO> requestApiKey(@RequestBody CreateApiKeyRequestDTO request) {
        // 사용자 정보 조회
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // 사용자 없으면 404 반환
        }

        // 사용자 API 키 발급 요청
        ApiKey apiKey = apiKeyService.issueApiKey(request.getName(), request.getAllowedIp(), user);  // User 객체 추가
        ApiKeyResponseDTO response = new ApiKeyResponseDTO();
        response.setId(apiKey.getId());
        response.setName(apiKey.getName());
        response.setActive(apiKey.isActive());
        response.setApiKey(apiKey.getApiKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ 사용자 API 키 조회
    @GetMapping("/mypage/apikey")
    public ResponseEntity<ApiKeyResponseDTO> getUserApiKey(@RequestParam Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ApiKey apiKey = apiKeyService.getApiKeyForUser(user);  // User 객체 추가
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

    // ✅ 사용자 API 키 발급 요청 기록 조회
    @GetMapping("/mypage/apikey-request")
    public ResponseEntity<ApiKeyResponseDTO> getUserApiKeyRequest(@RequestParam Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // 사용자 없으면 404 반환
        }

        ApiKey apiKey = apiKeyService.getApiKeyRequestForUser(user);  // 사용자가 발급 요청한 API 키 조회
        if (apiKey == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // 요청한 기록 없으면 404 반환
        }

        ApiKeyResponseDTO response = new ApiKeyResponseDTO();
        response.setId(apiKey.getId());
        response.setName(apiKey.getName());
        response.setActive(apiKey.isActive());
        response.setApiKey(apiKey.getApiKey());
        return ResponseEntity.ok(response);  // 요청 기록이 있으면 반환
    }

}
