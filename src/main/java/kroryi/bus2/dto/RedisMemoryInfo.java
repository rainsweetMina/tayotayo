package kroryi.bus2.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedisMemoryInfo {
    private String time;
    private double usedMemory;
    private double maxMemory;
    private int connectedClients;
    private long routesCount;
    private long requestToday;
}