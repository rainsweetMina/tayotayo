package kroryi.bus2.dto.mypage;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.SignupType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ModifyUserDTO {

    private String userId;

    @NotBlank(message = "이름을 입력하세요.")
    private String username;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "현재 비밀번호를 입력하세요.")
    private String currentPassword;

    // 읽기 전용 (optional)
    private LocalDate signupDate;
    private Role role;
    private SignupType signupType;
}
