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
     * @param name API 키 이름
     * @param allowedIp 허용된 IP
     * @param user 발급 대상 사용자
     * @return 발급된 API 키
     */
    @Transactional
    public ApiKey issueApiKey(String name, String allowedIp, User user) {
        // API 키 생성
        ApiKey apiKey = ApiKey.builder()
                .apikey(UUID.randomUUID().toString())  // 랜덤 API 키 생성
                .name(name)
                .allowedIp(allowedIp)
                .user(user)  // 사용자 정보 추가
                .expiresAt(LocalDateTime.now().plusDays(defaultExpirationDays))  // 기본 만료일 설정
                .status(ApiKeyStatus.PENDING)  // 기본 상태는 PENDING
                .createdAt(LocalDateTime.now())  // 생성일자 설정
                .build();

        // API 키를 DB에 저장하고 반환
        return apiKeyRepository.save(apiKey);
    }

    /**
     * 일반 사용자가 API 키를 신청하는 메서드
     * - 이미 PENDING 또는 APPROVED 상태의 키가 존재하면 신청 불가
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

        // 새로운 API 키 생성
        ApiKey key = ApiKey.builder()
                .user(user)
                .apikey(UUID.randomUUID().toString().replace("-", ""))  // 중복을 피한 랜덤 API 키 생성
                .status(ApiKeyStatus.PENDING)  // 상태를 PENDING으로 설정
                .createdAt(LocalDateTime.now())  // 생성일자 설정
                .build();

        apiKeyRepository.save(key);  // DB에 저장
    }

    // ====================================
    // 👤 사용자별 API 키 조회
    // ====================================

    /**
     * 사용자 ID로 가장 최근의 API 키 조회
     * @param userId 사용자 ID
     * @return 가장 최근의 API 키 (Optional)
     */
    public Optional<ApiKey> findLatestByUserId(String userId) {
        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return apiKeyRepository.findTopByUserOrderByCreatedAtDesc(user);  // 가장 최근 생성된 키 반환
    }

    /**
     * 로그인한 사용자(User 객체) 기준으로 가장 최근의 API 키 조회
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

    // ====================================
    // ✅ API 키 유효성 검사 (미사용)
    // ====================================

    /**
     * API 키 유효성 검사
     * @param apiKey 검증할 API 키
     * @return 유효한 API 키인지 여부 (미사용 중)
     */
    public boolean isValidApiKey(String apiKey) {
        // 실제 API 키 검증 로직 (DB에서 확인하거나 특정 값과 비교)
        return apiKeyRepository.existsByApikey(apiKey);
    }

    // API 키가 관리자인지 확인하는 메서드
    public boolean isAdminApiKey(String apiKey) {
        // 실제 관리자 API 키 확인 로직
        // 예시로 특정 API 키가 관리자인지 확인하는 조건을 추가
        return "admin-api-key".equals(apiKey); // 예시로 "admin-api-key"가 관리자 키라고 가정
    }
}
