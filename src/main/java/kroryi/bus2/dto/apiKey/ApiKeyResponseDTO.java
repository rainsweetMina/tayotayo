package kroryi.bus2.dto.apiKey;

import com.fasterxml.jackson.annotation.JsonFormat;
import kroryi.bus2.entity.apikey.ApiKey;
import kroryi.bus2.entity.apikey.ApiKeyStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiKeyResponseDTO {
    private Long id;
    private String username;
    private String userId;
    private boolean active;
    private String apiKey;
    private ApiKeyStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    public static ApiKeyResponseDTO fromEntity(ApiKey entity) {
        ApiKeyResponseDTO dto = new ApiKeyResponseDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUser().getUsername());
        dto.setUserId(entity.getUser().getUserId());
        dto.setActive(entity.isActive());
        dto.setApiKey(entity.getApiKey());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setExpiresAt(entity.getExpiresAt());
        return dto;
    }
}
