package kroryi.bus2.dto.apiKey;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 키 응답 DTO
 */
@Data
public class ApiKeyResponseDTO {
    private Long id;

    private String username;         // 사용자 이름
    private String userId;           // 사용자 ID (user_id 대신 일관성 유지)

    private boolean active;          // 활성 상태
    private String apiKey;           // 실제 API 키

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt; // 발급일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt; // 만료일
}
