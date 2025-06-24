package kroryi.bus2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private int totalUsers;
    private int newUsersToday;
    private int increaseRate; // 증가율(%)
    
    // 회원 타입별 수를 저장하는 맵 (USER, BUS, ADMIN)
    private Map<String, Integer> usersByType;
} 