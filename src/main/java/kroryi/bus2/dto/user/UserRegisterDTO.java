package kroryi.bus2.dto.user;

import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.SignupType;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "아이디를 입력하세요.")
    private String userId;

    @NotBlank(message = "이름을 입력하세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "유효한 이메일을 입력하세요.")
    private String email;

    private String phoneNumber;

    private Role role = Role.USER;

    private SignupType signupType = SignupType.GENERAL;
}
