package kroryi.bus2.service.admin;

import jakarta.transaction.Transactional;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.admin.AdminUserRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 모든 유저 리스트 조회
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 유저 아이디 또는 이름 기준 검색
     */
    public List<User> searchUsers(String keyword) {
        return adminUserRepository.findByUserIdContainingOrUsernameContaining(keyword, keyword);
    }

    /**
     * 유저 권한 변경
     */
    @Transactional
    public void changeUserRole(String userId, Role newRole) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setRole(newRole);
    }

    public LocalDate getSignupDate(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getSignupDate();
    }

    public LocalDateTime getLastLogin(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getLastLoginAt();
    }

    @Transactional
    public String generateTemporaryPassword(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 임시 비밀번호 생성
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // 비밀번호 암호화
        String encoded = passwordEncoder.encode(tempPassword);
        user.setPassword(encoded);

        log.info("관리자가 사용자 {}에게 임시 비밀번호를 발급했습니다.", userId);
        return tempPassword;  // 관리자가 사용자에게 전달 가능
    }

    @Transactional
    public void withdrawUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setWithdraw(true);
        log.info("관리자가 사용자 {}를 탈퇴 처리했습니다.", userId);
    }

}
