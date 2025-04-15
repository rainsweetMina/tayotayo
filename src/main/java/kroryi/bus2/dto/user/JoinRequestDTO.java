package kroryi.bus2.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    private String userId;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$",
            message = "비밀번호는 8자 이상이며, 영문/숫자/특수문자를 모두 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String passwordCheck;

    @NotBlank(message = "이름을 입력하세요.")
    private String username;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    // 인증 여부 (폼에선 hidden으로 true/false 값 전달)
    private boolean emailVerified = false;

    // 사용자가 입력한 인증 코드 (백엔드 인증 처리용)
    private String emailVerificationCode;

    private String phoneNumber;

    // 일반/소셜 회원가입 구분
    private SignupType signupType = SignupType.GENERAL;

    /**
     * 비밀번호 암호화 후 엔티티로 변환
     */
    public User toEntity(String encodedPassword) {
        return User.builder()
                .userId(this.userId)
                .password(encodedPassword)
                .username(this.username)
                .email(this.email)
                .phoneNumber(normalizePhoneNumber(this.phoneNumber))
                .signupType(this.signupType)
                .role(Role.USER)
                .signupDate(LocalDate.now())
                .build();
    }

    // 전화번호 하이픈(-) 제거
    private String normalizePhoneNumber(String phone) {
        return (phone != null) ? phone.replaceAll("-", "") : null;
    }

    // Boolean 타입 강제 Getter/Setter (Thymeleaf와의 호환을 위해)
    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = (emailVerified != null) && emailVerified;
    }


}
