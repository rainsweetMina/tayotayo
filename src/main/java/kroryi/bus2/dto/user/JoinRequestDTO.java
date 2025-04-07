package kroryi.bus2.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class JoinRequestDTO {

    @NotBlank(message = "아이디를 입력하세요.")
    private String userId;  // 로그인 아이디

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;

    @NotBlank(message = "비밀번호를 다시 입력하세요.")
    private String passwordCheck;

    @NotBlank(message = "이름을 입력하세요.")
    private String username;  // 닉네임 → 이름으로 변경

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private String phoneNumber;

    private SignupType signupType = SignupType.GENERAL; // 기본값 GENERAL

    // 비밀번호 암호화 후 엔티티로 변환
    public User toEntity(String encodedPassword) {
        return User.builder()
                .userId(this.userId)
                .password(encodedPassword)
                .username(this.username)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .signupType(this.signupType)
                .role(Role.USER)  // 기본 권한 USER
                .signupDate(LocalDate.now())  // 가입 날짜 설정
                .build();
    }
}
