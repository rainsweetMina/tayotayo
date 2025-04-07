package kroryi.bus2.dto.ad;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdStatsDTO {
    private long scheduledCount;   // 진행 예정
    private long ongoingCount;     // 진행 중
    private long endingSoonCount;  // 종료 임박
    private long endedCount;       // 종료됨
    private long deletedCount;     // 삭제됨

}
