package kroryi.bus2.dto.Route;

import kroryi.bus2.entity.Route;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link Route}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteListDTO implements Serializable {
    private Long id;
    private String routeId;
    private String routeNo;
    private String stNm;
    private String edNm;
    private String routeNote;
    private String dataconnareacd;
    private String routeTCd;
}