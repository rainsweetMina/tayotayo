package kroryi.bus2.service.admin;

import jakarta.transaction.Transactional;
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
        return user.getSignupDate().toLocalDate();
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
    
    /**
     * 회원 통계 정보를 제공합니다.
     * - 전체 회원 수
     * - 오늘 가입한 회원 수
     * - 증가율(%)
     * - 회원 타입별 수 (USER, BUS, ADMIN)
     */
    public UserStatsDTO getUserStats() {
        try {
            // 일반 회원(USER 권한) 수 조회
            long totalUsers = userRepository.count();
            long userCount = userRepository.countByRole(Role.USER);
            long busCount = userRepository.countByRole(Role.BUS);
            long adminCount = userRepository.countByRole(Role.ADMIN);
            
            // 오늘 가입한 일반 회원 수 조회
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);
            
            long newUsersToday = userRepository.countBySignupDateBetween(startOfDay, endOfDay);
            
            // 어제 총 회원 수 (오늘 회원 수 - 오늘 가입한 회원 수)
            long yesterdayUsers = totalUsers - newUsersToday;
            
            // 증가율 계산 (어제 회원이 0명이거나 오늘 가입한 회원이 0명이면 증가율은 0%로 설정)
            int increaseRate = (yesterdayUsers > 0 && newUsersToday > 0) 
                ? (int) Math.round((double) newUsersToday / yesterdayUsers * 100) 
                : 0;
            
            // 로그 추가
            log.info("회원 통계: 전체 회원 수={}, 오늘 가입={}, 어제 회원 수={}, 증가율={}%", 
                    totalUsers, newUsersToday, yesterdayUsers, increaseRate);
            
            // 회원 타입별 데이터 맵 생성
            Map<String, Integer> usersByType = Map.of(
                "USER", (int) userCount,
                "BUS", (int) busCount,
                "ADMIN", (int) adminCount
            );
            
            return UserStatsDTO.builder()
                    .totalUsers((int) totalUsers)
                    .newUsersToday((int) newUsersToday)
                    .increaseRate(increaseRate)
                    .usersByType(usersByType)
                    .build();
        } catch (Exception e) {
            log.error("회원 통계 조회 중 오류 발생: {}", e.getMessage());
            // 오류 발생 시 기본값 반환
            return UserStatsDTO.builder()
                    .totalUsers(0)
                    .newUsersToday(0)
                    .increaseRate(0)
                    .usersByType(Map.of("USER", 0, "BUS", 0, "ADMIN", 0))
                    .build();
        }
    }

    /**
     * 오늘 가입한 회원 목록을 조회합니다.
     */
    public List<User> getTodaySignupUsers() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);
        
        log.info("오늘 가입한 회원 조회: {} ~ {}", startOfDay, endOfDay);
        
        return userRepository.findBySignupDateBetween(startOfDay, endOfDay);
    }
}
