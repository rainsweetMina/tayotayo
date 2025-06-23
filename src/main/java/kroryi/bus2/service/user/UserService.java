package kroryi.bus2.service.user;

import jakarta.transaction.Transactional;
import kroryi.bus2.dto.user.JoinRequestDTO;
import kroryi.bus2.dto.user.LoginRequestDTO;
import kroryi.bus2.dto.mypage.ModifyUserDTO;
import kroryi.bus2.dto.user.UserInfoDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import kroryi.bus2.service.mypage.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public boolean checkUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public void join(JoinRequestDTO dto) {
        // ✅ 이메일 정규화
        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        dto.setEmail(normalizedEmail); // DTO 보정
        log.debug("📧 [회원가입] 정규화된 이메일: {}", normalizedEmail);

        // ✅ 백엔드 인증 여부 확인 (프론트 값이 아닌 실제 인증 저장소 확인)
        if (!emailService.isEmailVerified(normalizedEmail)) {
            throw new IllegalArgumentException("이메일 인증을 먼저 완료해주세요.");
        }

        // ✅ 아이디, 이메일 중복 확인
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // ✅ 비밀번호 확인
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (!isValidPassword(dto.getPassword())) {
            throw new IllegalArgumentException("비밀번호 조건이 맞지 않습니다.");
        }

        // ✅ 사용자 저장
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(encodedPassword);
        user.setEmail(normalizedEmail);

        userRepository.save(user);
        log.info("✅ 사용자 저장 성공: {}", user.getUserId());

        // ✅ 인증 상태 제거 (1회성)
        emailService.removeVerifiedEmail(normalizedEmail);
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

        notificationService.createNotification(
                userId,
                "회원 정보 변경 완료"
        );

        return true;
    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(regex);
    }

    @Transactional
    public boolean changePassword(String userId, String currentPassword, String newPassword, String confirmPassword) {
        User user = findByUserId(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 서로 일치하지 않습니다.");
        }

        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        return true;
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void updateLastLoginAt(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * ✅ Vue API에서 사용하는 탈퇴 - 비밀번호 검증 포함
     */
    @Transactional
    public void withdrawUser(String userId, String password) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.setWithdraw(true);
        userRepository.save(user);
    }

    /**
     * ✅ HTML 뷰에서 사용하는 탈퇴 - 비밀번호 검증 없이 바로 처리
     */
    @Transactional
    public void withdrawUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setWithdraw(true);
        userRepository.save(user);
    }

    public UserInfoDTO getUserInfo(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다."));

        return new UserInfoDTO(
                user.getId(),
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getSignupType(),
                user.getSignupDate(),
                user.getRole().name(),
                user.getLastLoginAt()
        );
    }

    @Transactional
    public void sendTemporaryPassword(String userId, String email) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디를 찾을 수 없습니다."));

        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("입력한 이메일이 회원정보와 일치하지 않습니다.");
        }

        String tempPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        emailService.sendTemporaryPassword(email, tempPassword);
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*#?&";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public boolean isUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }
}
