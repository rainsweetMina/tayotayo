package kroryi.bus2.service.user;

import jakarta.transaction.Transactional;
import kroryi.bus2.dto.user.JoinRequestDTO;
import kroryi.bus2.dto.user.LoginRequestDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean checkUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public void join(JoinRequestDTO dto) {
        if (dto.getEmailVerified() == null || !dto.getEmailVerified()) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }

        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (!isValidPassword(dto.getPassword())) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(encodedPassword);
        userRepository.save(user);
    }

    public User login(LoginRequestDTO ldto) {
        Optional<User> optionalUser = userRepository.findByUserId(ldto.getUserId());

        if (optionalUser.isEmpty()) {
            log.info("해당 아이디로 사용자를 찾을 수 없습니다.");
            return null;
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(ldto.getPassword(), user.getPassword())) {
            log.warn("비밀번호 불일치.");
            return null;
        }

        return user;
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void deleteByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }

    @Transactional
    public boolean modifyUserInfo(String userId, ModifyUserDTO dto) {
        User user = findByUserId(userId);
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        return true;
    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(regex);
    }

    @Transactional
    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        User user = findByUserId(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return true;
    }
}
