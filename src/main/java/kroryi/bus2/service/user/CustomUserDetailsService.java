package kroryi.bus2.service.user;

import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;      // ✅ Spring 트랜잭션

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 일반 로그인 시 호출됨
     */
    @Override
    @Transactional  // ✅ 조회 + 업데이트 한 번에 처리
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("🔑 loadUserByUsername called. userId = {}", username);

        // 1) 사용자 조회
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("❌ 사용자를 찾을 수 없습니다: " + username));

        // 2) 탈퇴 여부 확인
        if (user.isWithdraw()) {
            log.warn("🚫 탈퇴한 사용자 로그인 시도 - userId: {}", username);
            throw new DisabledException("이미 탈퇴한 계정입니다.");
        }

        // 3) 마지막 로그인 시각 업데이트
        userRepository.updateLastLoginAt(username, LocalDateTime.now());
        log.info("📝 lastLoginAt 업데이트 완료 - {}", username);

        // 4) UserDetails 반환
        return new CustomUserDetails(user);
    }
}
