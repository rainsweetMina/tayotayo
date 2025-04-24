package kroryi.bus2.dto.apikey;

import kroryi.bus2.entity.user.User;

/**
 * API 키 응답 DTO
 */
public class ApiKeyResponseDTO {
    private Long id;
    private String name;
    private boolean active;
    private String apiKey;
    private User user;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
