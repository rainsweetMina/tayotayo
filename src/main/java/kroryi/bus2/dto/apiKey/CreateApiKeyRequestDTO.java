package kroryi.bus2.dto.apiKey;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateApiKeyRequestDTO {
    private String name;
    private String allowedIp;
    private LocalDateTime expiresAt;
    private List<String> callbackUrls;
    private String userId;
}
