package kroryi.bus2.dto.Route;

import kroryi.bus2.entity.route.Route;
import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link Route}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteDTO implements Serializable {
    private String routeId;
    private String routeNo;
    private String stBsId;
    private String edBsId;
    private String stNm;
    private String edNm;
    private String routeNote;
    private String dataconnareacd;
    private String dirRouteNote;
    private String ndirRouteNote;
    private String routeTCd;
}