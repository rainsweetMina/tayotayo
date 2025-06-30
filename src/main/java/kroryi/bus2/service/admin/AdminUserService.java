package kroryi.bus2.service.admin;

import jakarta.transaction.Transactional;
import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.aop.AdminTracked;
import kroryi.bus2.dto.UserStatsDTO;
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
import java.util.Map;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    /* ─────────── 조회 메서드 ─────────── */
    public List<User> getAllUsers() { return userRepository.findAll(); }
    public List<User> searchUsers(String keyword) { return adminUserRepository.findByUserIdContainingOrUsernameContaining(keyword, keyword); }
    public LocalDate getSignupDate(String userId) { return userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")).getSignupDate().toLocalDate(); }
    public LocalDateTime getLastLogin(String userId) { return userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")).getLastLoginAt(); }

    /* ─────────── 관리자 액션 메서드 ─────────── */
    @Transactional
    @AdminAudit(action = "권한 변경", target = "User")
    public void changeUserRole(String userId, Role newRole) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setRole(newRole);
        log.info("권한 변경: {} → {}", userId, newRole);
    }

    @Transactional
    @AdminAudit(action = "임시 비밀번호 발급", target = "User")
    public String generateTemporaryPassword(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        log.info("임시 비밀번호 발급: {}", userId);
        return tempPassword;
    }

    @Transactional
    @AdminAudit(action = "회원 탈퇴 처리", target = "User")
    public void withdrawUser(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setWithdraw(true);
        log.info("탈퇴 처리: {}", userId);
    }

    /* ─────────── 통계 메서드 ─────────── */
    public UserStatsDTO getUserStats() { /* 기존 로직 그대로 */ return null; }
    public List<User> getTodaySignupUsers() { /* 기존 로직 그대로 */ return null; }
}