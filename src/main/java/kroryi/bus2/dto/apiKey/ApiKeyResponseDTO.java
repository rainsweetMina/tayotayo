package kroryi.bus2.dto.apiKey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kroryi.bus2.entity.user.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 키 응답 DTO
 */
@Data
//-------------- 추후 수정 --------------------------------------------------------
public class ApiKeyResponseDTO {
    private Long id;
    @JsonIgnore
    private String name;
    @JsonIgnore
    private boolean active;
    private String apiKey;
    @JsonIgnore
    private User user;
    private LocalDateTime createdAt;
    @JsonIgnore
    private LocalDateTime expiresAt;

//    // Getters and Setters
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public boolean isActive() {
//        return active;
//    }
//
//    public void setActive(boolean active) {
//        this.active = active;
//    }
//
//    public String getApiKey() {
//        return apiKey;
//    }
//
//    public void setApiKey(String apiKey) {
//        this.apiKey = apiKey;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//    }
}
