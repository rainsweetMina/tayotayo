package kroryi.bus2.repository.jpa.user;

import kroryi.bus2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
