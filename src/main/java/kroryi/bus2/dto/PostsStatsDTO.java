package kroryi.bus2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostsStatsDTO {
    private PostTypeStats notices;
    private PostTypeStats qna;
    private PostTypeStats advertisements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostTypeStats {
        private int today;
        private int total;
    }
} 