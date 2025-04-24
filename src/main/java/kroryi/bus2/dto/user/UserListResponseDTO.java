package kroryi.bus2.dto.user;

import kroryi.bus2.entity.user.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserListResponseDTO {
    private String userId;
    private String username;
    private String email;
    private Role role;
}
