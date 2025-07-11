package kroryi.bus2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisStats {
    private long usedMemory;
    private long maxMemory;
    private int connectedClients;
    private String uptime;
    private String version;
    private String error;

    public static RedisStats createError(String errorMessage) {
        return RedisStats.builder().error(errorMessage).build();
    }
}