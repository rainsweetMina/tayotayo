package kroryi.bus2.dto.Route;

import kroryi.bus2.entity.Route;
import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link Route}
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteDTO implements Serializable {
    private Long id;
    private String routeId;
    private String routeNo;
    private String stBsId;
    private String edBsId;
    private String stNm;
    private String edNm;
    private String routeNote;
}