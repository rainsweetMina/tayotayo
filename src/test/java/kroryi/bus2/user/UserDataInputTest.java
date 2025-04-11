package kroryi.bus2.user;

import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.user.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@SpringBootTest
@Log4j2
public class UserDataInputTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void insertTestUser() {
        String userId = "testuser";

        if (userRepository.findByUserId(userId).isPresent()) {
            log.info("이미 존재하는 아이디 입니다 : {}", userId);
            return;
        }

        User user = User.builder()
                .userId(userId)
                .username("테스트 유저")
                .password(passwordEncoder.encode("1234"))
                .email("testuser@example.com")
                .phoneNumber("010-1234-5678")
                .role(Role.USER)
                .signupType(SignupType.GENERAL)
                .signupDate(LocalDate.now())
                .build();

        userRepository.save(user);
        log.info("테스트 유저 저장 완료: {} / {}", user.getUserId(), "1234");
    }
}
