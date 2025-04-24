package kroryi.bus2.dto.user;

import kroryi.bus2.entity.user.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRoleChangeResponseDTO {
    private String userId;
    private Role newRole;
    private String message;
}
