package kroryi.bus2.repository.jpa;

import org.springframework.transaction.annotation.Transactional;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 아이디 중복 확인
    boolean existsByUserId(String userId);

    // userId로 회원 탈퇴
    void deleteByUserId(String userId);

    // 로그인 및 사용자 조회용
    Optional<User> findByUserId(String userId);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // 유저 이름 검색용 (예: QnA 검색)
    List<User> findByUsernameContaining(String keyword);

    // 탈퇴하지 않은 회원 전체 조회
    List<User> findByWithdrawFalse();

    // 특정 기간 내 가입한 회원 수
    long countBySignupDateBetween(LocalDateTime start, LocalDateTime end);

    // 특정 권한을 가진 회원 수
    long countByRole(Role role);

    // 특정 권한 & 특정 기간 내 가입 회원 수
    long countByRoleAndSignupDateBetween(Role role, LocalDateTime start, LocalDateTime end);

    // 특정 기간 내 가입한 회원 목록
    List<User> findBySignupDateBetween(LocalDateTime start, LocalDateTime end);

    // 🕓 최근 로그인 시간 갱신 (JPQL 업데이트)
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)  // ✅ flush 추가
    @Query("update User u set u.lastLoginAt = :now where u.userId = :userId")
    void updateLastLoginAt(@Param("userId") String userId,
                           @Param("now") LocalDateTime now);
}
