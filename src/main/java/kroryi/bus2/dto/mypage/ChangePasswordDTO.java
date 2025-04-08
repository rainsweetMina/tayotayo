package kroryi.bus2.dto.mypage;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {

    @NotBlank(message = "현재 비밀번호를 입력하세요.")
    private String currentPassword;

    @NotBlank(message = "변경할 비밀번호를 입력하세요.")
    private String modifyPassword;

    @NotBlank(message = "변경할 비밀번호를 다시 입력하세요.")
    private String modifyPasswordCheck;
}
