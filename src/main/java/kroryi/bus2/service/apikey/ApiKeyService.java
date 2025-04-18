package kroryi.bus2.service.apikey;

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

    @Value("${apikey.default.expiration}")
    private int defaultExpirationDays;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, UserRepository userRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApiKey issueApiKey(String name, String allowedIp) {
        ApiKey apiKey = ApiKey.builder()
                .apikey(UUID.randomUUID().toString())
                .name(name)
                .allowedIp(allowedIp)
                .expiresAt(LocalDateTime.now().plusDays(defaultExpirationDays))
                .build();
        return apiKeyRepository.save(apiKey);
    }

    // ApiKeyService.java
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

        // 새로운 API 키 생성
        ApiKey key = ApiKey.builder()
                .user(user)
                .apikey(UUID.randomUUID().toString().replace("-", ""))  // API 키 생성
                .status(ApiKeyStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        apiKeyRepository.save(key);  // DB에 저장
    }

    public Optional<ApiKey> findLatestByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return apiKeyRepository.findTopByUserOrderByCreatedAtDesc(user);
    }

    // 사용자의 가장 최근 API 키를 반환하는 메서드
    public ApiKey getApiKeyForUser(User user) {
        // 가장 최근에 발급된 API 키를 조회
        return apiKeyRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);  // 만약 API 키가 없다면 null 반환
    }

    // 모든 API Key 목록을 최근 생성 순으로 조회
    public List<ApiKey> getAllApiKeys() {
        return apiKeyRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));  // createdAt 기준으로 내림차순 정렬
    }

    // API 키 상태를 토글하는 메서드 (활성화/비활성화)
    @Transactional
    public boolean toggleActive(Long id) {
        // API 키 조회
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API 키를 찾을 수 없습니다."));

        // 상태에 따른 활성화/비활성화 처리
        if (apiKey.getStatus() == ApiKeyStatus.PENDING) {
            apiKey.setStatus(ApiKeyStatus.APPROVED);  // PENDING → APPROVED
        } else if (apiKey.getStatus() == ApiKeyStatus.APPROVED) {
            apiKey.setStatus(ApiKeyStatus.PENDING);  // APPROVED → PENDING
        }

        apiKeyRepository.save(apiKey);

        // 상태에 따라 active 판단 (예: APPROVED → true)
        return apiKey.getStatus() == ApiKeyStatus.APPROVED;
    }
}
