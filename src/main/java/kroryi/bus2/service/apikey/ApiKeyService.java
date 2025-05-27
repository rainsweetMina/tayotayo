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

    // ====================================
    // 🔐 API 키 발급 / 신청 처리
    // ====================================

    /**
     * 새로운 API 키 발급
     *
     * @param user_name      API 키 이름
     * @param allowedIp 허용된 IP
     * @param user      발급 대상 사용자
     * @return 발급된 API 키
     */
    @Transactional
    public ApiKey issueApiKey(String user_name, String allowedIp, User user) {

        ApiKey apiKey = ApiKey.builder()
                .apikey(UUID.randomUUID().toString())
                .allowedIp(allowedIp)
                .user(user)                                // ← User 객체
                .userIdString(user.getUserId())            // ← String 형태의 로그인 ID 복사 저장
                .user_name(user_name)
                .expiresAt(LocalDateTime.now().plusDays(defaultExpirationDays))
                .status(ApiKeyStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return apiKeyRepository.save(apiKey);
    }


    /**
     * 일반 사용자가 API 키를 신청하는 메서드
     * - 이미 PENDING 또는 APPROVED 상태의 키가 존재하면 신청 불가
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void requestApiKey(String userId) {
        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        log.info("사용자 ID: {}에 대해 API 키 신청 처리 중", userId);

        // 이미 신청한 상태인지 확인
        boolean alreadyRequested = apiKeyRepository.existsByUserAndStatus(user, ApiKeyStatus.PENDING);
        boolean alreadyApproved = apiKeyRepository.existsByUserAndStatus(user, ApiKeyStatus.APPROVED);

        log.info("API 키 상태 체크: 요청 여부 - {}, 승인 여부 - {}", alreadyRequested, alreadyApproved);

        if (alreadyRequested || alreadyApproved) {
            throw new IllegalStateException("이미 API 키를 신청했거나 발급받은 상태입니다.");
        }

        // 기본값으로 API 키 발급 처리
        ApiKey apiKey = issueApiKey(
                user.getUsername(),        // 기본 이름
                "0.0.0.0",           // 기본 허용 IP (모두 허용)
                user
        );

        log.info("🔑 API 키 발급 완료 - 키: {}", apiKey.getApiKey());
    }


    public void renewApiKey(String userId) {
        // 1. User 객체 먼저 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. User 객체로 ApiKey 조회
        ApiKey apiKey = apiKeyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("기존 API 키가 없습니다."));

        // 3. 키 갱신
        apiKey.setApikey(UUID.randomUUID().toString().replace("-", ""));  // 새 키로 설정
        apiKey.setStatus(ApiKeyStatus.PENDING); // 다시 승인 대기로

        apiKeyRepository.save(apiKey);
    }


    // ====================================
    // 👤 사용자별 API 키 조회
    // ====================================

    /**
     * 사용자 ID로 가장 최근의 API 키 조회
     *
     * @param userId 사용자 ID
     * @return 가장 최근의 API 키 (Optional)
     */
    public Optional<ApiKey> findLatestByUserId(String userId) {
        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return apiKeyRepository.findTopByUserAndStatusOrderByCreatedAtDesc(user, ApiKeyStatus.APPROVED);

    }

    /**
     * 로그인한 사용자(User 객체) 기준으로 가장 최근의 API 키 조회
     *
     * @param user 사용자 객체
     * @return 가장 최근의 API 키
     */
    public ApiKey getApiKeyForUser(User user) {
        // 로그인한 사용자 기준 가장 최근 API 키 조회
        return apiKeyRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);  // 없으면 null 반환
    }

    // ====================================
    // 📋 전체 API 키 목록 조회 (관리자 전용)
    // ====================================

    /**
     * 관리자 페이지에서 모든 API 키 목록을 조회 (내림차순으로 정렬)
     *
     * @return 모든 API 키 목록
     */
    public List<ApiKey> getAllApiKeys() {
        return apiKeyRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));  // 생성일 기준 내림차순 정렬
    }

    // ====================================
    // 🔁 API 키 상태 토글 / 승인 처리
    // ====================================

    /**
     * API 키 상태를 승인(PENDING → APPROVED) 또는 비승인(승인 취소) 처리
     *
     * @param id API 키 ID
     * @return 승인된 상태인지 여부 (true: 승인, false: 대기)
     */
    @Transactional
    public boolean toggleActive(Long id) {
        // API 키 조회
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API 키를 찾을 수 없습니다."));

        // 상태 토글 처리
        if (apiKey.getStatus() == ApiKeyStatus.PENDING) {
            apiKey.setStatus(ApiKeyStatus.APPROVED);  // 대기 → 승인
        } else if (apiKey.getStatus() == ApiKeyStatus.APPROVED) {
            apiKey.setStatus(ApiKeyStatus.PENDING);  // 승인 → 대기
        }

        apiKeyRepository.save(apiKey);  // 상태 변경 후 저장
        return apiKey.getStatus() == ApiKeyStatus.APPROVED;  // 상태가 승인된 경우 true 반환
    }

    // ====================================
    // ✅ API 키 재발급 기능
    // ====================================

    /**
     * 사용자가 기존 API 키를 재발급 받는 기능
     *
     * @param userId 사용자 ID
     * @return 재발급된 API 키
     */
    @Transactional
    public ApiKey reissueApiKey(String userId) {
        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        log.info("사용자 ID: {}에 대해 API 키 재발급 처리 중", userId);

        // 기존 API 키가 있는지 확인
        apiKeyRepository.findByUserAndStatus(user, ApiKeyStatus.PENDING)
                .ifPresent(apiKey -> {
                    throw new IllegalStateException("기존 API 키가 아직 승인 대기 중입니다.");
                });
        apiKeyRepository.findByUserAndStatus(user, ApiKeyStatus.APPROVED)
                .ifPresent(apiKey -> {
                    throw new IllegalStateException("기존 API 키가 이미 승인되었습니다.");
                });

        // 새로운 API 키 발급
        ApiKey newApiKey = ApiKey.builder()
                .user(user)
                .apikey(UUID.randomUUID().toString().replace("-", ""))  // 중복을 피한 랜덤 API 키 생성
                .status(ApiKeyStatus.PENDING)  // 상태를 PENDING으로 설정
                .createdAt(LocalDateTime.now())  // 생성일자 설정
                .build();

        // 새로운 API 키 저장
        return apiKeyRepository.save(newApiKey);
    }

    public ApiKey getApiKeyRequestForUser(User user) {
        return apiKeyRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);
    }

    @Transactional
    public void toggleActiveStatus(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API 키를 찾을 수 없습니다: " + id));
        apiKey.setActive(!apiKey.isActive()); // 활성화 상태 토글
        apiKeyRepository.save(apiKey);
    }

    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

}
