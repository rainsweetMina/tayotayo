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
    private String userId;  // 로그인 ID

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String passwordCheck;

    @NotBlank(message = "이름을 입력하세요.")
    private String username;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private String emailVerificationCode; // 사용자가 입력한 이메일 인증 코드

    private boolean emailVerified = false; // 인증 완료 여부 (기본 false)

    private String phoneNumber;

    private SignupType signupType = SignupType.GENERAL; // 소셜/일반 회원가입 구분

    /**
     * 비밀번호 암호화 이후, 엔티티로 변환
     */
    public User toEntity(String encodedPassword) {
        return User.builder()
                .userId(this.userId)
                .password(encodedPassword)
                .username(this.username)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .signupType(this.signupType)
                .role(Role.USER)  // 일반 사용자 권한
                .signupDate(LocalDate.now())
                .build();
    }

    // emailVerified 값을 반환하는 getter 메서드
    public Boolean getEmailVerified() {
        return emailVerified;  // emailVerified 값을 반환
    }

    // emailVerified 값을 설정하는 setter 메서드
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;  // emailVerified 값을 설정
    }

}
