package kroryi.bus2.dto.user;

import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserListResponseDTO {

    private Long id;
    private String userId;
    private String username;
    private String email;
    private String phoneNumber;
    private LocalDateTime signupDate;
    private LocalDateTime lastLoginAt;
    private SignupType signupType;
    private Role role;
    private boolean withdraw;

    public static UserListResponseDTO from(User user) {
        return new UserListResponseDTO(
                user.getId(),
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getSignupDate(),
                user.getLastLoginAt(),
                user.getSignupType(),
                user.getRole(),
                user.isWithdraw()
        );
    }
}
