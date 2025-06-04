package kroryi.bus2.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FavoriteBusStopResponseDTO {
    private String bsId;
    private String bsNm; // 정류장 이름
}
