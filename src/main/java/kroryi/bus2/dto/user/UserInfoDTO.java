package kroryi.bus2.dto.user;

import kroryi.bus2.entity.user.SignupType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserInfoDTO {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private SignupType signupType;
    private LocalDateTime signupDate;
    private String role;
    private LocalDateTime lastLoginAt;
}
