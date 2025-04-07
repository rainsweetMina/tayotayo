package kroryi.bus2.service;

import kroryi.bus2.dto.user.JoinRequestDTO;
import kroryi.bus2.dto.user.LoginRequestDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 아이디 중복 확인
    public boolean checkUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }

    // 회원가입
    public void join(JoinRequestDTO jdto) {
        String encodedPassword = passwordEncoder.encode(jdto.getPassword());
        userRepository.save(jdto.toEntity(encodedPassword));
    }

    // 로그인
    public User login(LoginRequestDTO ldto) {
        Optional<User> optionalUser = userRepository.findByUserId(ldto.getUserId());

        if (optionalUser.isEmpty()) {
            return null;  // 아이디가 없으면 로그인 실패
        }

        User user = optionalUser.get();


        if (!passwordEncoder.matches(ldto.getPassword(), user.getPassword())) {
            return null;  // 비밀번호 불일치 시 로그인 실패
        }

        return user;
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public void deleteByUserId(String userId) {
        userRepository.deleteByUserId(userId);
    }

}
