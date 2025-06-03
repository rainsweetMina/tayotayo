package kroryi.bus2.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FavoriteRouteResponseDTO {
    private String routeId;
    private String routeNo;
    private String stNm;
    private String edNm;
}

