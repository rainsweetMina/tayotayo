package kroryi.bus2.dto.lost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LostItemStatsDTO {
    private long lostItemCount;     // 분실물 신고 수
    private long foundItemCount;    // 습득물 등록 수
    private long matchedCount;      // 매칭 건 수
    private long unmatchedCount;    // 미매칭 건 수
}

