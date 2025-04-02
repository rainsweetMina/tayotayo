package kroryi.bus2.dto.lost;

import lombok.Data;

@Data
public class LostFoundMatchRequestDTO {
    private Long lostItemId;
    private Long foundItemId;
    private Long matchedById; // 매칭한 관리자 (User)
}

