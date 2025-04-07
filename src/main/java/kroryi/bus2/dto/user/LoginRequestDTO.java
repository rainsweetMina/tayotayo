package kroryi.bus2.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class LoginRequestDTO {

    private String userId;
    private String password;
}
