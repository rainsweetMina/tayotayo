package kroryi.bus2.service.user;

import kroryi.bus2.config.security.CustomUserDetails;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 일반 로그인 시 호출됨
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("🔐 로그인 시도 - userId: {}", username);

        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("❌ 사용자를 찾을 수 없습니다: " + username));

        log.info("✅ 사용자 조회 성공 - username: {}", user.getUsername());
        return new CustomUserDetails(user);
    }
}
