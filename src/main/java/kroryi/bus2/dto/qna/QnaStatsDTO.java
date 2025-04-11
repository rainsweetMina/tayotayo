package kroryi.bus2.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QnaStatsDTO {
    private long totalCount;
    private long waitingCount;
    private long todayCount;
    private long hiddenCount;
    private long secretCount;
}

