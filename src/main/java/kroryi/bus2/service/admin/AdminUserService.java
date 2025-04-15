package kroryi.bus2.service.admin;

import jakarta.transaction.Transactional;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.admin.AdminUserRepository;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;

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
}
