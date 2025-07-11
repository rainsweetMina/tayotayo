package kroryi.bus2.service.apikey;

import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.aop.AdminTracked;
import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.apikey.ApiKeyStatus;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import kroryi.bus2.repository.jpa.apikey.ApiKeyRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    @Value("${apikey.default.expiration:365}")
    private int defaultExpirationDays;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, UserRepository userRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApiKey issueApiKey(String user_name, String allowedIp, User user) {
        List<ApiKey> existingKeys = apiKeyRepository.findAllByUser(user);
        for (ApiKey oldKey : existingKeys) {
            oldKey.setActive(false);
            oldKey.setStatus(ApiKeyStatus.PENDING); // EXPIRED 대신 PENDING 사용
        }
        apiKeyRepository.flush();

        ApiKey apiKey = ApiKey.builder()
                .apikey(UUID.randomUUID().toString())
                .allowedIp(allowedIp)
                .user(user)
                .userIdString(user.getUserId())
                .user_name(user_name)
                .expiresAt(LocalDateTime.now().plusYears(1))
                .status(ApiKeyStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        return apiKeyRepository.save(apiKey);
    }

    @Transactional
    public void requestApiKey(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        log.info("사용자 ID: {}에 대해 API 키 신청 처리 중", userId);

        boolean alreadyRequested = apiKeyRepository.existsByUserAndStatus(user, ApiKeyStatus.PENDING);
        boolean alreadyApproved = apiKeyRepository.existsByUserAndStatus(user, ApiKeyStatus.APPROVED);

        log.info("API 키 상태 체크: 요청 여부 - {}, 승인 여부 - {}", alreadyRequested, alreadyApproved);

        if (alreadyRequested || alreadyApproved) {
            throw new IllegalStateException("이미 API 키를 신청했거나 발급받은 상태입니다.");
        }

        ApiKey apiKey = issueApiKey(user.getUsername(), "0.0.0.0", user);
        log.info("🔑 API 키 발급 완료 - 키: {}", apiKey.getApiKey());
    }

    @Transactional
    public ApiKey renewApiKey(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<ApiKey> activeKeys = apiKeyRepository.findAllByUserAndActiveTrue(user);
        activeKeys.forEach(key -> {
            key.setActive(false);
            key.setStatus(ApiKeyStatus.PENDING); // EXPIRED 대신 PENDING 사용
        });
        apiKeyRepository.flush();

        ApiKey newApiKey = ApiKey.builder()
                .user(user)
                .apikey(UUID.randomUUID().toString())
                .status(ApiKeyStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusYears(1))
                .active(true)
                .userIdString(user.getUserId())
                .user_name(user.getUsername())
                .build();

        return apiKeyRepository.save(newApiKey);
    }

    public Optional<ApiKey> findLatestByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return apiKeyRepository.findTopByUserOrderByCreatedAtDesc(user);
    }

    public ApiKey getApiKeyForUser(User user) {
        return apiKeyRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);
    }

    public List<ApiKey> getAllApiKeys() {
        return apiKeyRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));
    }

    @Transactional
    @AdminAudit(action = "API 키 상태 전환", target = "ApiKey")
    public boolean toggleActive(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API 키를 찾을 수 없습니다."));

        if (apiKey.getStatus() == ApiKeyStatus.PENDING) {
            apiKey.setStatus(ApiKeyStatus.APPROVED);
        } else if (apiKey.getStatus() == ApiKeyStatus.APPROVED) {
            apiKey.setStatus(ApiKeyStatus.PENDING);
        }

        apiKeyRepository.save(apiKey);
        return apiKey.getStatus() == ApiKeyStatus.APPROVED;
    }

    @Transactional
    public ApiKey reissueApiKey(String userId) {
        log.info("🔁 [ApiKeyService] reissueApiKey 시작 - userId: {}", userId);
        
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        log.info("🔁 [ApiKeyService] 사용자 조회 성공 - userId: {}", userId);

        List<ApiKey> activeKeys = apiKeyRepository.findAllByUserAndActiveTrue(user);
        log.info("🔁 [ApiKeyService] 기존 활성 API 키 개수: {}", activeKeys.size());
        
        activeKeys.forEach(key -> {
            key.setActive(false);
            key.setStatus(ApiKeyStatus.PENDING); // EXPIRED 대신 PENDING 사용
            log.info("🔁 [ApiKeyService] 기존 API 키 비활성화 - apiKeyId: {}", key.getId());
        });
        apiKeyRepository.flush();

        ApiKey newApiKey = ApiKey.builder()
                .user(user)
                .apikey(UUID.randomUUID().toString())
                .status(ApiKeyStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusYears(1))
                .active(true)
                .userIdString(user.getUserId())
                .user_name(user.getUsername())
                .build();

        ApiKey savedApiKey = apiKeyRepository.save(newApiKey);
        log.info("🔁 [ApiKeyService] 새 API 키 생성 완료 - apiKeyId: {}", savedApiKey.getId());
        
        return savedApiKey;
    }

    public ApiKey getApiKeyRequestForUser(User user) {
        return apiKeyRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);
    }

    @Transactional
    public void toggleActiveStatus(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API 키를 찾을 수 없습니다: " + id));
        apiKey.setActive(!apiKey.isActive());
        apiKeyRepository.save(apiKey);
    }

    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public Optional<ApiKey> findById(Long id) {
        return apiKeyRepository.findById(id);
    }
}
