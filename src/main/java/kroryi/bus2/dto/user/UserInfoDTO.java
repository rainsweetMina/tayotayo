package kroryi.bus2.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import kroryi.bus2.entity.user.SignupType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserInfoDTO {
    private Long id; // ✅ 추가
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private SignupType signupType;
    private LocalDateTime signupDate;
    private String role;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;
}
