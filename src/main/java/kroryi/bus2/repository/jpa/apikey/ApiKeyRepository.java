package kroryi.bus2.repository.jpa.apikey;

import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.apikey.ApiKeyStatus;
import kroryi.bus2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    // ====================================
    // 🔍 중복 확인 및 상태 기반 조회
    // ====================================

    /**
     * 특정 사용자에 대해 특정 상태(PENDING 또는 APPROVED)의 API 키가 존재하는지 확인
     */
    boolean existsByUserAndStatus(User user, ApiKeyStatus apiKeyStatus);

    // ====================================
    // 📦 최근 API 키 조회
    // ====================================

    /**
     * 특정 사용자의 가장 최근에 생성된 API 키 조회
     */


    // 사용자와 상태를 기반으로 API 키 조회
    Optional<ApiKey> findByUserAndStatus(User user, ApiKeyStatus status);

    Optional<ApiKey> findTopByUserOrderByCreatedAtDesc(User user);

    // ====================================
    // 🔑 API 키 문자열 기반 조회
    // ====================================

    /**
     * API 키 문자열로 APIKey 엔티티 조회
     */
    ApiKey findByApikey(String apikey);

    boolean existsByApikey(String apikey);

    Optional<ApiKey> findFirstByUser(User user);

    Optional<ApiKey> findByUser(User user);

    Optional<ApiKey> findTopByUserAndStatusOrderByCreatedAtDesc(User user, ApiKeyStatus apiKeyStatus);


    List<ApiKey> findAllByUser(User user);

    List<ApiKey> findAllByUserAndActiveTrue(User user);
}
