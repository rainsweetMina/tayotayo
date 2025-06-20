package kroryi.bus2.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenDTO {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
} 