package kroryi.bus2.dto.Route;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomRouteDTO {

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