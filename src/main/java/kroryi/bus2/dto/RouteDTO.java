package kroryi.bus2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link kroryi.bus2.entity.Route}
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
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