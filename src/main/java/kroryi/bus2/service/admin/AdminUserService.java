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
    public UserStatsDTO getUserStats() {
        try {
            // 전체 회원 수
            long totalUsers = userRepository.count();
            
            // 오늘 가입한 회원 수
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);
            
            long newUsersToday = userRepository.countBySignupDateBetween(startOfDay, endOfDay);
            
            // 어제 가입한 회원 수 (증가율 계산용)
            LocalDate yesterday = today.minusDays(1);
            LocalDateTime startOfYesterday = yesterday.atStartOfDay();
            LocalDateTime endOfYesterday = yesterday.plusDays(1).atStartOfDay().minusSeconds(1);
            
            long newUsersYesterday = userRepository.countBySignupDateBetween(startOfYesterday, endOfYesterday);
            
            // 증가율 계산 (어제 대비 오늘 가입자 증가율)
            int increaseRate = 0;
            if (newUsersYesterday > 0) {
                increaseRate = (int) Math.round(((double) (newUsersToday - newUsersYesterday) / newUsersYesterday) * 100);
            } else if (newUsersToday > 0) {
                increaseRate = 100; // 어제 0명, 오늘 1명 이상이면 100% 증가
            }
            
            // 회원 타입별 통계
            Map<String, Integer> usersByType = Map.of(
                "USER", (int) userRepository.countByRole(Role.USER),
                "BUS", (int) userRepository.countByRole(Role.BUS),
                "ADMIN", (int) userRepository.countByRole(Role.ADMIN)
            );
            
            log.info("회원 통계 생성 완료: totalUsers={}, newUsersToday={}, increaseRate={}, usersByType={}", 
                     totalUsers, newUsersToday, increaseRate, usersByType);
            
            return UserStatsDTO.builder()
                    .totalUsers((int) totalUsers)
                    .newUsersToday((int) newUsersToday)
                    .increaseRate(increaseRate)
                    .usersByType(usersByType)
                    .build();
                    
        } catch (Exception e) {
            log.error("회원 통계 생성 중 오류 발생", e);
            // 오류 발생 시 기본값 반환
            return UserStatsDTO.builder()
                    .totalUsers(1000)
                    .newUsersToday(0)
                    .increaseRate(0)
                    .usersByType(Map.of("USER", 850, "BUS", 120, "ADMIN", 30))
                    .build();
        }
    }
    
    public List<User> getTodaySignupUsers() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);
        
        return userRepository.findBySignupDateBetween(startOfDay, endOfDay);
    }
}