package kroryi.bus2.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRequestDTO {
    private String userId;
    private String email;
}
