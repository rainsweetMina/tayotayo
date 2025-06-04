package kroryi.bus2.dto.mypage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FavoriteRouteRequestDTO {
    private String userId;
    private String routeId;
}
