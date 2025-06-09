package kroryi.bus2.repository.jpa.apikey;

import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.apikey.ApiKeyStatus;
import kroryi.bus2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    // ✅ 중복 확인
    boolean existsByUserAndStatus(User user, ApiKeyStatus apiKeyStatus);

    // ✅ 특정 사용자 기준 최신 키 1개
    Optional<ApiKey> findTopByUserOrderByCreatedAtDesc(User user);

    // ✅ 상태 기준 + 최신 키 1개
    Optional<ApiKey> findTopByUserAndStatusOrderByCreatedAtDesc(User user, ApiKeyStatus apiKeyStatus);

    // ✅ 문자열 기준 조회
    ApiKey findByApikey(String apikey);

    boolean existsByApikey(String apikey);

    // ✅ 사용자 전체 키 조회
    List<ApiKey> findAllByUser(User user);

    // ✅ 사용자 활성 키만 조회
    List<ApiKey> findAllByUserAndActiveTrue(User user);
}