package kroryi.bus2.dto.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    // ✅ 이메일 인증 여부
    private Boolean emailVerified = false;

    // ✅ 프론트에서 verificationCode 또는 emailVerificationCode로 보내도 받도록 처리
    @JsonAlias({"emailVerificationCode", "verificationCode"})
    private String verificationCode;

    // 선택 사항
    private String phoneNumber;

    private SignupType signupType = SignupType.GENERAL;

    public User toEntity(String encodedPassword) {
        return User.builder()
                .userId(this.userId)
                .password(encodedPassword)
                .username(this.username)
                .email(this.email)
                .phoneNumber(normalizePhoneNumber(this.phoneNumber))
                .signupType(this.signupType)
                .role(Role.USER)
                .signupDate(LocalDateTime.now())
                .build();
    }

    private String normalizePhoneNumber(String phone) {
        return (phone != null) ? phone.replaceAll("-", "") : null;
    }

    // Boolean 강제 getter (Thymeleaf 호환용)
    public Boolean getEmailVerified() {
        return emailVerified != null && emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = (emailVerified != null) && emailVerified;
    }
}
