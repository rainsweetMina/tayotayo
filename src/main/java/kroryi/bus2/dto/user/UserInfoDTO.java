package kroryi.bus2.dto.user;

import kroryi.bus2.entity.user.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoDTO {
    private String userId;
    private String username;
    private String email;
    private Role role;
}
